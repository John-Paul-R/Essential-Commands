package com.fibermc.essentialcommands.commands;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.fibermc.essentialcommands.EssentialCommands;
import com.fibermc.essentialcommands.ManagerLocator;
import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.database.JoinpointDatabase;
import com.fibermc.essentialcommands.playerdata.PlayerData;
import com.fibermc.essentialcommands.text.ECText;
import com.fibermc.essentialcommands.text.TextFormatType;
import com.fibermc.essentialcommands.types.JoinpointLocation;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

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

    abstract class JoinpointShareException extends RuntimeException {
        private final String joinpointName;

        public JoinpointShareException(String joinpointName) {
            this.joinpointName = joinpointName;
        }

        public String getJoinpointName() {
            return joinpointName;
        }
    }

    final class JoinpointNotFoundException extends JoinpointShareException {
        public JoinpointNotFoundException(String joinpointName) {
            super(joinpointName);
        }
    }

    final class JoinpointAlreadyGlobalException extends JoinpointShareException {
        public JoinpointAlreadyGlobalException(String joinpointName) {
            super(joinpointName);
        }
    }

    final class NoNewPlayersException extends JoinpointShareException {
        public NoNewPlayersException(String joinpointName) {
            super(joinpointName);
        }
    }

    final class PlayersNotSharedException extends JoinpointShareException {
        public PlayersNotSharedException(String joinpointName) {
            super(joinpointName);
        }
    }

    final class CannotClearGlobalException extends JoinpointShareException {
        public CannotClearGlobalException(String joinpointName) {
            super(joinpointName);
        }
    }

    private <T> CompletableFuture<T> async(Supplier<T> supplier, Consumer<Function<ECText, Text>> sendError)
    {
        return CompletableFuture
            .supplyAsync(supplier, Executors.newVirtualThreadPerTaskExecutor())
            .exceptionallyAsync(threadException -> {
                if (!(threadException instanceof CompletionException)) {
                    EssentialCommands.LOGGER.error(threadException);
                    sendError.accept(ecText -> ecText.getText(
                        "cmd.joinpoint.error.unknown",
                        TextFormatType.Error,
                        Text.literal(threadException.getMessage())
                    ));
                }
                Function<ECText, Text> errorFunction = switch (threadException.getCause()) {
                    case JoinpointNotFoundException e -> ecText -> ecText.getText(
                        "cmd.joinpoint.share.error.not_found",
                        TextFormatType.Error,
                        Text.literal(e.getJoinpointName())
                    );
                    case JoinpointAlreadyGlobalException e -> ecText -> ecText.getText(
                        "cmd.joinpoint.share.error.already_global",
                        TextFormatType.Error,
                        ecText.accent(e.getJoinpointName())
                    );
                    case NoNewPlayersException e -> ecText -> ecText.getText(
                        "cmd.joinpoint.share.error.no_new_players",
                        TextFormatType.Error
                    );
                    case PlayersNotSharedException e -> ecText -> ecText.getText(
                        "cmd.joinpoint.share.error.players_not_shared",
                        TextFormatType.Error
                    );
                    case CannotClearGlobalException e -> ecText -> ecText.getText(
                        "cmd.joinpoint.share.error.cannot_clear_global",
                        TextFormatType.Error,
                        ecText.accent(e.getJoinpointName())
                    );
                    default -> {
                        EssentialCommands.LOGGER.error("Unknown error in a Joinpoint share command", threadException);
                        yield ecText -> ecText.getText(
                            "cmd.joinpoint.error.unknown",
                            TextFormatType.Error,
                            Text.literal(threadException.getCause().getMessage())
                        );
                    }
                };
                sendError.accept(errorFunction);
                return null;
            }, Util.getMainWorkerExecutor());
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

        async(() -> {
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
            throw new JoinpointNotFoundException(joinpointName);
        }

        if (joinpoint.isGlobal()) {
            throw new JoinpointAlreadyGlobalException(joinpointName);
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
            throw new NoNewPlayersException(joinpointName);
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
            throw new JoinpointNotFoundException(joinpointName);
        }

        Set<UUID> newSharedWith = new HashSet<>(joinpoint.getSharedWith());
        Set<String> removedPlayerNames = new HashSet<>();

        for (var targetPlayer : targetPlayers) {
            if (newSharedWith.remove(targetPlayer.getUuid())) {
                removedPlayerNames.add(targetPlayer.getName().getString());
            }
        }

        if (removedPlayerNames.isEmpty()) {
            throw new PlayersNotSharedException(joinpointName);
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
            throw new JoinpointNotFoundException(joinpointName);
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
            throw new JoinpointNotFoundException(joinpointName);
        }

        if (joinpoint.isGlobal()) {
            throw new CannotClearGlobalException(joinpointName);
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
