package com.fibermc.essentialcommands.commands;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import com.fibermc.essentialcommands.ManagerLocator;
import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.database.JoinpointDatabase;
import com.fibermc.essentialcommands.playerdata.PlayerData;
import com.fibermc.essentialcommands.teleportation.PlayerTeleporter;
import com.fibermc.essentialcommands.text.ECText;
import com.fibermc.essentialcommands.text.TextFormatType;
import com.fibermc.essentialcommands.types.JoinpointLocation;
import com.fibermc.essentialcommands.types.MinecraftLocation;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class JoinpointTpCommand implements Command<ServerCommandSource> {

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity senderPlayer = context.getSource().getPlayerOrThrow();
        String ownerName = StringArgumentType.getString(context, "owner_player");
        String joinpointName = StringArgumentType.getString(context, "joinpoint_name");

        return exec(senderPlayer, ownerName, joinpointName);
    }

    public int runOwnJoinpoint(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity senderPlayer = context.getSource().getPlayerOrThrow();
        String joinpointName = StringArgumentType.getString(context, "joinpoint_name");

        return exec(senderPlayer, senderPlayer.getName().getString(), joinpointName);
    }

    private int exec(ServerPlayerEntity senderPlayer, String ownerName, String joinpointName)
                    throws CommandSyntaxException {

        PlayerData senderPlayerData = ((ServerPlayerEntityAccess) senderPlayer).ec$getPlayerData();
        JoinpointDatabase database = ManagerLocator.getInstance().getJoinpointDatabase();

        try {
            UUID ownerUuid;

            // If owner is self, use sender's UUID
            if (ownerName.equals(senderPlayer.getName().getString())) {
                ownerUuid = senderPlayer.getUuid();
            } else {
                // Try to find owner by name from online players first
                ServerPlayerEntity ownerPlayer = null;
                for (ServerPlayerEntity player : senderPlayer.getServer().getPlayerManager().getPlayerList()) {
                    if (player.getName().getString().equals(ownerName)) {
                        ownerPlayer = player;
                        break;
                    }
                }

                if (ownerPlayer == null) {
                    Message msg = ECText.access(senderPlayer).getText(
                        "cmd.joinpoint.tp.error.owner_not_found",
                        TextFormatType.Error,
                        Text.literal(ownerName)
                    );
                    throw new CommandSyntaxException(new SimpleCommandExceptionType(msg), msg);
                }
                ownerUuid = ownerPlayer.getUuid();
            }

            // Get the joinpoint
            JoinpointLocation joinpoint = database.getJoinpoint(joinpointName, ownerUuid);

            if (joinpoint == null) {
                Message msg = ECText.access(senderPlayer).getText(
                    "cmd.joinpoint.tp.error.not_found",
                    TextFormatType.Error,
                    Text.literal(joinpointName),
                    Text.literal(ownerName)
                );
                throw new CommandSyntaxException(new SimpleCommandExceptionType(msg), msg);
            }

            // Check if player can access this joinpoint
            if (!joinpoint.canAccess(senderPlayer)) {
                Message msg = ECText.access(senderPlayer).getText(
                    "cmd.joinpoint.tp.error.no_access",
                    TextFormatType.Error,
                    Text.literal(joinpointName),
                    Text.literal(ownerName)
                );
                throw new CommandSyntaxException(new SimpleCommandExceptionType(msg), msg);
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

            // Convert JoinpointLocation to MinecraftLocation for teleportation
            MinecraftLocation teleportLocation = new MinecraftLocation(
                joinpoint.dim(),
                joinpoint.x(),
                joinpoint.y(),
                joinpoint.z(),
                joinpoint.headYaw(),
                joinpoint.pitch()
            );

            PlayerTeleporter.requestTeleport(senderPlayerData, teleportLocation, locationText);

        } catch (SQLException e) {
            senderPlayerData.sendCommandError("cmd.joinpoint.error.database", Text.literal(e.getMessage()));
            return 0;
        }

        return SINGLE_SUCCESS;
    }

    public static class Suggestion {

        // Suggestion provider for accessible joinpoints
        public static final SuggestionProvider<ServerCommandSource> ACCESSIBLE_JOINPOINTS =
            (context, builder) -> {
                try {
                    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                    JoinpointDatabase database = ManagerLocator.getInstance().getJoinpointDatabase();
                    List<JoinpointLocation> joinpoints = database.getAccessibleJoinpoints(player);

                    for (JoinpointLocation joinpoint : joinpoints) {
                        builder.suggest(joinpoint.getName());
                    }
                } catch (Exception ignored) {
                    // Fail silently for suggestions
                }

                return builder.buildFuture();
            };

        // Suggestion provider for owned joinpoints
        public static final SuggestionProvider<ServerCommandSource> OWNED_JOINPOINTS =
            (context, builder) -> {
                try {
                    ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
                    JoinpointDatabase database = ManagerLocator.getInstance().getJoinpointDatabase();
                    List<JoinpointLocation> joinpoints = database.getOwnedJoinpoints(player.getUuid());

                    for (JoinpointLocation joinpoint : joinpoints) {
                        builder.suggest(joinpoint.getName());
                    }
                } catch (Exception ignored) {
                    // Fail silently for suggestions
                }

                return builder.buildFuture();
            };

        // Suggestion provider for joinpoints owned by a specific player
        public static final SuggestionProvider<ServerCommandSource> OWNER_JOINPOINTS =
            (context, builder) -> {
                var requesterPlayer = context.getSource().getPlayerOrThrow();

                JoinpointDatabase database = ManagerLocator.getInstance().getJoinpointDatabase();
                List<JoinpointLocation> joinpoints;
                try {
                    joinpoints = database.getOwnedJoinpoints(requesterPlayer.getUuid());
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }

                for (JoinpointLocation joinpoint : joinpoints) {
                    builder.suggest(joinpoint.getName());
                }

                return builder.buildFuture();
            };
    }
}
