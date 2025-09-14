package com.fibermc.essentialcommands.commands.joinpoints;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import com.fibermc.essentialcommands.ManagerLocator;
import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.database.JoinpointDatabase;
import com.fibermc.essentialcommands.playerdata.PlayerData;
import com.fibermc.essentialcommands.text.ECText;
import com.fibermc.essentialcommands.types.JoinpointLocation;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class JoinpointShareCommand implements Command<ServerCommandSource> {

    private final Action action;

    public enum Action {
        ADD, REMOVE, LIST, CLEAR
    }

    public JoinpointShareCommand(Action action) {
        this.action = action;
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String joinpointName = StringArgumentType.getString(context, "joinpoint_name");
        return exec(context, joinpointName);
    }

    private Consumer<Function<ECText, Text>> sendErrorToPlayer(ServerPlayerEntity senderPlayer) {
        var ecText = ECText.access(senderPlayer);

        return (messageFn) -> {
            var message = messageFn.apply(ecText);
            PlayerData.access(senderPlayer)
                .sendCommandError(message);
        };
    }

    private int exec(CommandContext<ServerCommandSource> context, String joinpointName) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity senderPlayer = source.getPlayerOrThrow();

        // Extract target players outside async for ADD/REMOVE actions
        Collection<ServerPlayerEntity> targetPlayers = null;
        if (action == Action.ADD || action == Action.REMOVE) {
            targetPlayers = EntityArgumentType.getPlayers(context, "target_players");
        }

        // Capture targetPlayers for lambda
        final Collection<ServerPlayerEntity> finalTargetPlayers = targetPlayers;

        Async.runCommand(() -> {
            PlayerData playerData = ((ServerPlayerEntityAccess) senderPlayer).ec$getPlayerData();
            JoinpointDatabase database = ManagerLocator.getInstance().getJoinpointDatabase();

            return switch (action) {
                case ADD -> handleAddAsync(finalTargetPlayers, joinpointName, senderPlayer, playerData, database);
                case REMOVE -> handleRemoveAsync(finalTargetPlayers, joinpointName, senderPlayer, playerData, database);
                case LIST -> handleListAsync(joinpointName, senderPlayer, playerData, database);
                case CLEAR -> handleClearAsync(joinpointName, senderPlayer, playerData, database);
            };
        }, sendErrorToPlayer(senderPlayer));

        return SINGLE_SUCCESS;
    }

    private Void handleAddAsync(
        Collection<ServerPlayerEntity> targetPlayers,
        String joinpointName,
        ServerPlayerEntity senderPlayer,
        PlayerData playerData,
        JoinpointDatabase database
    )
    {
        JoinpointLocation joinpoint = database.getJoinpointAsync(joinpointName, senderPlayer.getUuid()).join();
        if (joinpoint == null) {
            throw new JoinpointException.NotFound(joinpointName);
        }

        if (joinpoint.isGlobal()) {
            throw new JoinpointException.AlreadyGlobal(joinpointName);
        }

        Set<UUID> newSharedWith = new HashSet<>(joinpoint.getSharedWith());
        Set<String> addedPlayerNames = new HashSet<>();

        for (var targetPlayer : targetPlayers) {
            if (!targetPlayer.getUuid().equals(senderPlayer.getUuid())) {
                if (newSharedWith.add(targetPlayer.getUuid())) {
                    addedPlayerNames.add(targetPlayer.getName().getString());
                }
            }
        }

        if (addedPlayerNames.isEmpty()) {
            throw new JoinpointException.NoNewPlayers(joinpointName);
        }

        JoinpointLocation updatedJoinpoint = new JoinpointLocation(
            joinpoint, joinpoint.getName(), joinpoint.getOwner(), false, newSharedWith
        );

        database.updateJoinpointAsync(joinpointName, senderPlayer.getUuid(), updatedJoinpoint).join();

        Text joinpointNameText = ECText.access(senderPlayer).accent(joinpointName);
        Text playersText = Text.literal(String.join(", ", addedPlayerNames));
        playerData.sendCommandFeedback("cmd.joinpoint.share.add.feedback", joinpointNameText, playersText);

        return null;
    }

    private Void handleRemoveAsync(
        Collection<ServerPlayerEntity> targetPlayers,
        String joinpointName,
        ServerPlayerEntity senderPlayer,
        PlayerData playerData,
        JoinpointDatabase database
    )
    {
        JoinpointLocation joinpoint = database.getJoinpointAsync(joinpointName, senderPlayer.getUuid()).join();
        if (joinpoint == null) {
            throw new JoinpointException.NotFound(joinpointName);
        }

        Set<UUID> newSharedWith = new HashSet<>(joinpoint.getSharedWith());
        Set<String> removedPlayerNames = new HashSet<>();

        for (var targetPlayer : targetPlayers) {
            if (newSharedWith.remove(targetPlayer.getUuid())) {
                removedPlayerNames.add(targetPlayer.getName().getString());
            }
        }

        if (removedPlayerNames.isEmpty()) {
            throw new JoinpointException.PlayersNotShared(joinpointName);
        }

        JoinpointLocation updatedJoinpoint = new JoinpointLocation(
            joinpoint, joinpoint.getName(), joinpoint.getOwner(), joinpoint.isGlobal(), newSharedWith
        );

        database.updateJoinpointAsync(joinpointName, senderPlayer.getUuid(), updatedJoinpoint).join();

        Text joinpointNameText = ECText.access(senderPlayer).accent(joinpointName);
        Text playersText = Text.literal(String.join(", ", removedPlayerNames));
        playerData.sendCommandFeedback("cmd.joinpoint.share.remove.feedback", joinpointNameText, playersText);

        return null;
    }

    private Void handleListAsync(
        String joinpointName,
        ServerPlayerEntity senderPlayer,
        PlayerData playerData,
        JoinpointDatabase database
    )
    {
        JoinpointLocation joinpoint = database.getJoinpointAsync(joinpointName, senderPlayer.getUuid()).join();
        if (joinpoint == null) {
            throw new JoinpointException.NotFound(joinpointName);
        }

        Text joinpointNameText = ECText.access(senderPlayer).accent(joinpointName);

        if (joinpoint.isGlobal()) {
            playerData.sendCommandFeedback("cmd.joinpoint.share.list.global", joinpointNameText);
            return null;
        }

        if (joinpoint.getSharedWith().isEmpty()) {
            playerData.sendCommandFeedback("cmd.joinpoint.share.list.private", joinpointNameText);
            return null;
        }

        Set<String> sharedPlayerNames = new HashSet<>();
        JoinpointListCommand.getSharedWithNames(database, joinpoint, sharedPlayerNames);

        Text playersText = Text.literal(String.join(", ", sharedPlayerNames));
        playerData.sendCommandFeedback("cmd.joinpoint.share.list.shared", joinpointNameText, playersText);

        return null;
    }

    private Void handleClearAsync(
        String joinpointName,
        ServerPlayerEntity senderPlayer,
        PlayerData playerData,
        JoinpointDatabase database
    )
    {
        JoinpointLocation joinpoint = database.getJoinpointAsync(joinpointName, senderPlayer.getUuid()).join();
        if (joinpoint == null) {
            throw new JoinpointException.NotFound(joinpointName);
        }

        if (joinpoint.isGlobal()) {
            throw new JoinpointException.CannotClearGlobal(joinpointName);
        }

        JoinpointLocation updatedJoinpoint = new JoinpointLocation(
            joinpoint, joinpoint.getName(), joinpoint.getOwner(), false, new HashSet<>()
        );

        database.updateJoinpointAsync(joinpointName, senderPlayer.getUuid(), updatedJoinpoint).join();

        Text joinpointNameText = ECText.access(senderPlayer).accent(joinpointName);
        playerData.sendCommandFeedback("cmd.joinpoint.share.clear.feedback", joinpointNameText);

        return null;
    }
}
