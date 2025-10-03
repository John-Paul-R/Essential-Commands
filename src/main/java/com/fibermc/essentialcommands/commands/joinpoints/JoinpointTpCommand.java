package com.fibermc.essentialcommands.commands.joinpoints;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fibermc.essentialcommands.EssentialCommands;
import com.fibermc.essentialcommands.ManagerLocator;
import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.database.JoinpointDatabase;
import com.fibermc.essentialcommands.playerdata.PlayerData;
import com.fibermc.essentialcommands.teleportation.PlayerTeleporter;
import com.fibermc.essentialcommands.text.ECText;
import com.fibermc.essentialcommands.text.TextFormatType;
import com.fibermc.essentialcommands.types.JoinpointLocation;
import com.fibermc.essentialcommands.types.NamedMinecraftLocation;
import org.apache.commons.lang3.function.TriFunction;
import org.apache.logging.log4j.message.ParameterizedMessage;
import org.jetbrains.annotations.Nullable;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class JoinpointTpCommand implements Command<ServerCommandSource> {

    @Override
    public int run(CommandContext<ServerCommandSource> context)
        throws CommandSyntaxException
    {
        ServerPlayerEntity senderPlayer = context.getSource().getPlayerOrThrow();
        String ownerName = StringArgumentType.getString(context, "owner_player");
        String joinpointName = StringArgumentType.getString(context, "joinpoint_name");

        return exec(senderPlayer, ownerName, joinpointName);
    }

    public int runOwnJoinpoint(CommandContext<ServerCommandSource> context)
        throws CommandSyntaxException
    {
        ServerPlayerEntity senderPlayer = context.getSource().getPlayerOrThrow();
        String joinpointName = StringArgumentType.getString(context, "joinpoint_name");

        return exec(senderPlayer, senderPlayer.getName().getString(), joinpointName);
    }

    private Consumer<Function<ECText, Text>> sendErrorToPlayer(ServerPlayerEntity senderPlayer) {
        var ecText = ECText.access(senderPlayer);

        return (messageFn) -> {
            var message = messageFn.apply(ecText);
            PlayerData.access(senderPlayer)
                .sendCommandError(message);
        };
    }

    /**
     * I really don't know how to mark that this has db calls (and therefore
     * must be run in a virthreads context) other than `async`
     */
    private @Nullable UUID getJoinpointOwnerIdAsync(ServerPlayerEntity senderPlayer, String ownerName)
    {
        // If owner is self, use sender's UUID
        if (ownerName.equals(senderPlayer.getName().getString())) {
            return senderPlayer.getUuid();
        }

        { // Try to find owner by name from online players first
            var ownerPlayer = senderPlayer.getEntityWorld().getServer().getPlayerManager().getPlayer(ownerName);
            if (ownerPlayer != null) {
                return ownerPlayer.getUuid();
            }
        }

        { // Go to our cached list of all player names as a last resort (particularly for offline players)
            JoinpointDatabase database = ManagerLocator.getInstance().getJoinpointDatabase();
            var ownerUuid = database.getOwnerPlayerIdByNameAsync(ownerName, senderPlayer.getUuid()).join();
            if (ownerUuid != null) {
                return ownerUuid;
            }
        }

        throw new JoinpointException.OwnerNotFound(ownerName);
    }

    private int exec(ServerPlayerEntity senderPlayer, String ownerName, String joinpointName)
    {
        Async.runCommand(() -> {
            PlayerData senderPlayerData = ((ServerPlayerEntityAccess) senderPlayer).ec$getPlayerData();
            JoinpointDatabase database = ManagerLocator.getInstance().getJoinpointDatabase();

            var ownerUuid = getJoinpointOwnerIdAsync(senderPlayer, ownerName);

            JoinpointLocation joinpoint = database.getJoinpointAsync(joinpointName, ownerUuid).join();

            if (joinpoint == null) {
                throw new JoinpointException.TpNotFound(joinpointName, ownerName);
            }

            if (!joinpoint.canAccess(senderPlayer)) {
                throw new JoinpointException.TpNoAccess(joinpointName, ownerName);
            }

            // Teleport & chat message
            var ecText = ECText.access(senderPlayer);

            // Add owner info if not self
            var locationText = !ownerUuid.equals(senderPlayer.getUuid())
                ? ecText.getText(
                "cmd.joinpoint.location_name_with_owner",
                TextFormatType.Default,
                ecText.accent(joinpointName),
                ecText.accent(ownerName)
            )
                : ecText.getText(
                "cmd.joinpoint.location_name",
                TextFormatType.Default,
                ecText.accent(joinpointName)
            );

            PlayerTeleporter.requestTeleport(senderPlayerData, joinpoint, locationText);

            return null;
        }, sendErrorToPlayer(senderPlayer));

        return SINGLE_SUCCESS;
    }

    public static class Suggestion {
        private static SuggestionProvider<ServerCommandSource> suggestFromDb(
            String name,
            TriFunction<JoinpointDatabase, ServerPlayerEntity, CommandContext<ServerCommandSource>, CompletableFuture<List<String>>> getSuggestionsAsync
        ) {
            return (context, builder) -> {
                try {
                    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                    JoinpointDatabase database = ManagerLocator.getInstance().getJoinpointDatabase();
                    return getSuggestionsAsync.apply(database, player, context)
                        .thenApply(items -> {
                            for (String item : items) {
                                builder.suggest(item);
                            }
                            return builder.build();
                        });
                } catch (Exception err) {
                    EssentialCommands.LOGGER.error(
                        new ParameterizedMessage("Error while suggesting {0} joinpoints", name),
                        err);
                    // continue with 0 suggestions
                    return builder.buildFuture();
                }
            };
        }

        public static final SuggestionProvider<ServerCommandSource> ACCESSIBLE_JOINPOINTS = suggestFromDb(
            "accessible",
            (db, player, ctx) -> db.getAccessibleJoinpointsAsync(player)
                .thenApply(el -> el.stream().map(NamedMinecraftLocation::getName).collect(Collectors.toList()))
        );

        public static final SuggestionProvider<ServerCommandSource> OWNERS_OF_ACCESSIBLE_JOINPOINTS = suggestFromDb(
            "accessible-owners",
            (db, player, ctx) -> db.getAccessibleJoinpointsOwnerNamesAsync(player)
        );

        public static final SuggestionProvider<ServerCommandSource> OWNED_JOINPOINTS = suggestFromDb(
            "owned",
            (db, player, ctx) -> db.getOwnedJoinpointsAsync(player.getUuid())
                .thenApply(el -> el.stream().map(NamedMinecraftLocation::getName).collect(Collectors.toList()))
        );

        public static final SuggestionProvider<ServerCommandSource> ACCESSIBLE_TARGET_PLAYER_JOINPOINTS = suggestFromDb(
            "accessible-for-owner",
            (db, player, ctx) -> db
                .getOwnerPlayerIdByNameAsync(StringArgumentType.getString(ctx, OWNER_PLAYER_ARG), player.getUuid())
                .thenCompose(ownerId -> ownerId == null
                    ? CompletableFuture.completedFuture(List.of())
                    : db.getAccessibleOwnedJoinpointsAsync(player, ownerId)
                )
                .thenApply(joinpoints -> joinpoints.stream().map(NamedMinecraftLocation::getName).collect(Collectors.toList()))
        );

    }

    public static final String OWNER_PLAYER_ARG = "owner_player";
}
