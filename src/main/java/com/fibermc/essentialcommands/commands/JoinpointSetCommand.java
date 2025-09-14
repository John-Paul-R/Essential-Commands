package com.fibermc.essentialcommands.commands;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.fibermc.essentialcommands.ECPerms;
import com.fibermc.essentialcommands.EssentialCommands;
import com.fibermc.essentialcommands.ManagerLocator;
import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.database.JoinpointDatabase;
import com.fibermc.essentialcommands.playerdata.PlayerData;
import com.fibermc.essentialcommands.text.ChatConfirmationPrompt;
import com.fibermc.essentialcommands.text.ECText;
import com.fibermc.essentialcommands.text.TextFormatType;
import com.fibermc.essentialcommands.types.JoinpointLimit;
import com.fibermc.essentialcommands.types.JoinpointLocation;
import com.fibermc.essentialcommands.types.MinecraftLocation;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

import dev.jpcode.eccore.util.CollectionUtils;

public class JoinpointSetCommand implements Command<ServerCommandSource> {

    private final Action action;

    public enum Action {
        SET, OVERWRITE, DELETE
    }

    public JoinpointSetCommand(Action action) {
        this.action = action;
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String joinpointName = StringArgumentType.getString(context, "joinpoint_name");
        return exec(context, joinpointName);
    }

    abstract class JoinpointSetException extends RuntimeException {
        private final String joinpointName;

        public JoinpointSetException(String joinpointName) {
            this.joinpointName = joinpointName;
        }

        public String getJoinpointName() {
            return joinpointName;
        }
    }

    final class JoinpointDeleteNotFoundException extends JoinpointSetException {
        public JoinpointDeleteNotFoundException(String joinpointName) {
            super(joinpointName);
        }
    }

    final class JoinpointMaxPointsExceededException extends JoinpointSetException {
        private final int max;
        private final int current;

        public JoinpointMaxPointsExceededException(String joinpointName, int max, int current) {
            super(joinpointName);
            this.max = max;
            this.current = current;
        }

        public int getMax() {
            return max;
        }

        public int getCurrent() {
            return current;
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
                    case JoinpointDeleteNotFoundException e -> ecText -> ecText.getText(
                        "cmd.joinpoint.delete.error",
                        TextFormatType.Error,
                        ecText.accent(e.getJoinpointName())
                    );
                    case JoinpointMaxPointsExceededException e -> ecText -> ecText.getText(
                        "cmd.joinpoint.set.error.limit",
                        TextFormatType.Error,
                        ecText.accent(e.getJoinpointName()),
                        Text.literal(String.valueOf(e.getMax()))
                    );
                    default -> {
                        EssentialCommands.LOGGER.error("Unknown error in a Joinpoint set command", threadException);
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

        // Extract global flag outside async (only relevant for SET and OVERWRITE)
        Boolean isGlobal = null;
        if (action == Action.SET || action == Action.OVERWRITE) {
            try {
                isGlobal = BoolArgumentType.getBool(context, "global");
            } catch (IllegalArgumentException ignored) {
                // Optional parameter not provided - will be false
                isGlobal = false;
            }
        }

        // Capture for lambda
        final Boolean finalIsGlobal = isGlobal;

        async(() -> {
            PlayerData playerData = ((ServerPlayerEntityAccess) senderPlayer).ec$getPlayerData();
            JoinpointDatabase database = ManagerLocator.getInstance().getJoinpointDatabase();

            return switch (action) {
                case SET -> handleSetAsync(finalIsGlobal, joinpointName, senderPlayer, playerData, database);
                case OVERWRITE -> handleOverwriteAsync(finalIsGlobal, joinpointName, senderPlayer, playerData, database);
                case DELETE -> handleDeleteAsync(joinpointName, senderPlayer, playerData, database);
            };
        }, sendErrorToPlayer(senderPlayer));

        return SINGLE_SUCCESS;
    }

    private Void handleSetAsync(
        boolean isGlobal,
        String joinpointName,
        ServerPlayerEntity senderPlayer,
        PlayerData playerData,
        JoinpointDatabase database
    )
    {
        var joinpoints = database.getOwnedJoinpointsAsync(senderPlayer.getUuid()).join();

        boolean exists = joinpoints.stream().anyMatch(joinpoint -> joinpoint.getName().equalsIgnoreCase(joinpointName));

        if (exists) {
            // Ask the player whether they want to override the joinpoint
            ECText playerEcText = ECText.access(senderPlayer);
            playerData.sendMessage(
                "cmd.joinpoint.set.overwrite",
                playerEcText.accent(joinpointName)
            );

            new ChatConfirmationPrompt(
                senderPlayer,
                "/joinpoint overwrite " + joinpointName,
                playerEcText.accent("[" + ECText.getInstance().getString("generic.confirm") + "]")
            ).send();
            return null;
        }

        JoinpointLimit.JoinpointType joinpointType = isGlobal
            ? JoinpointLimit.JoinpointType.GLOBAL
            : JoinpointLimit.JoinpointType.SHARED;

        var targetTypePerms = ECPerms.Registry.Group.joinpoint_limit_groups.get(joinpointType);
        var anyTypePerms = ECPerms.Registry.Group.joinpoint_limit_groups.get(JoinpointLimit.JoinpointType.ANY);

        int playerMaxJoinpoints = ECPerms.getHighestNumericPermission(
            senderPlayer.getCommandSource(),
            CollectionUtils.concat(targetTypePerms, anyTypePerms));

        if (joinpoints.size() >= playerMaxJoinpoints) {
            throw new JoinpointMaxPointsExceededException(joinpointName, playerMaxJoinpoints, joinpoints.size());
        }

        // Create new joinpoint
        MinecraftLocation location = new MinecraftLocation(senderPlayer);
        JoinpointLocation joinpoint = new JoinpointLocation(
            location, joinpointName, senderPlayer.getUuid(), isGlobal, Set.of()
        );

        database.createJoinpointAsync(joinpointName, senderPlayer.getUuid(), joinpoint).join();

        Text joinpointNameText = ECText.access(senderPlayer).accent(joinpointName);
        String messageKey = isGlobal ? "cmd.joinpoint.set.feedback.global"
            : "cmd.joinpoint.set.feedback";

        playerData.sendCommandFeedback(messageKey, joinpointNameText);

        return null;
    }

    private Void handleOverwriteAsync(
        boolean isGlobal,
        String joinpointName,
        ServerPlayerEntity senderPlayer,
        PlayerData playerData,
        JoinpointDatabase database
    )
    {
        Set<UUID> sharedWith = new HashSet<>();

        MinecraftLocation location = new MinecraftLocation(senderPlayer);
        JoinpointLocation joinpoint = new JoinpointLocation(
            location, joinpointName, senderPlayer.getUuid(), isGlobal, sharedWith
        );

        boolean exists = database.joinpointExistsAsync(joinpointName, senderPlayer.getUuid()).join();

        if (exists) {
            database.updateJoinpointAsync(joinpointName, senderPlayer.getUuid(), joinpoint).join();
        } else {
            database.createJoinpointAsync(joinpointName, senderPlayer.getUuid(), joinpoint).join();
        }

        Text joinpointNameText = ECText.access(senderPlayer).accent(joinpointName);
        String messageKey = isGlobal ? "cmd.joinpoint.overwrite.feedback.global"
            : "cmd.joinpoint.overwrite.feedback";

        playerData.sendCommandFeedback(messageKey, joinpointNameText);

        return null;
    }

    private Void handleDeleteAsync(
        String joinpointName,
        ServerPlayerEntity senderPlayer,
        PlayerData playerData,
        JoinpointDatabase database
    )
    {
        boolean wasSuccessful = database.deleteJoinpointAsync(joinpointName, senderPlayer.getUuid()).join();

        Text joinpointNameText = ECText.access(senderPlayer).accent(joinpointName);
        if (wasSuccessful) {
            playerData.sendCommandFeedback("cmd.joinpoint.delete.feedback", joinpointNameText);
        } else {
            throw new JoinpointDeleteNotFoundException(joinpointName);
        }

        return null;
    }
}
