package com.fibermc.joinpoints.commands;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;

import com.fibermc.joinpoints.JoinpointsPerms;
import com.fibermc.joinpoints.Joinpoints;
import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.joinpoints.database.JoinpointDatabase;
import com.fibermc.essentialcommands.playerdata.PlayerData;
import com.fibermc.essentialcommands.text.ChatConfirmationPrompt;
import com.fibermc.essentialcommands.text.ECText;
import com.fibermc.joinpoints.types.JoinpointLimit;
import com.fibermc.joinpoints.types.JoinpointLocation;
import com.fibermc.essentialcommands.types.MinecraftLocation;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

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

        Async.runCommand(() -> {
            PlayerData playerData = ((ServerPlayerEntityAccess) senderPlayer).ec$getPlayerData();
            JoinpointDatabase database = Joinpoints.getDatabase();

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

        var targetTypePerms = JoinpointsPerms.Registry.Group.joinpoint_limit_groups.get(joinpointType);
        var anyTypePerms = JoinpointsPerms.Registry.Group.joinpoint_limit_groups.get(JoinpointLimit.JoinpointType.ANY);

        int playerAllowedOfAnyType = anyTypePerms.length == 0 ? -1 : JoinpointsPerms.getHighestNumericPermission(senderPlayer.getCommandSource(), anyTypePerms);
        int playerAllowedCountOfTargetType = targetTypePerms.length == 0 ? -1 : JoinpointsPerms.getHighestNumericPermission(senderPlayer.getCommandSource(), targetTypePerms);

        // any(5) -> up to 5 shared or global, any combindation
        // any(5),shared(3) -> no more then 3 shared. Could have 5 global:0 shared to 2 global:3 shared

        boolean targetTypeIsGlobal = joinpointType == JoinpointLimit.JoinpointType.GLOBAL;
        int joinpointsOfTargetType = (int)joinpoints.stream().filter(p -> p.isGlobal() == targetTypeIsGlobal).count();

        if (playerAllowedCountOfTargetType != -1) {
            // if we have an explicit permission for the target type, that overrides all others
            if (joinpointsOfTargetType >= playerAllowedCountOfTargetType) {
                throw new JoinpointException.Set.MaxPointsExceeded(joinpointName, playerAllowedCountOfTargetType, joinpointsOfTargetType, joinpointType);
            }
        } else if (playerAllowedOfAnyType != -1 && joinpoints.size() >= playerAllowedOfAnyType) {
            throw new JoinpointException.Set.MaxPointsExceeded(joinpointName, playerAllowedOfAnyType, joinpoints.size(), JoinpointLimit.JoinpointType.ANY);
        }

        // Create new joinpoint
        MinecraftLocation location = new MinecraftLocation(senderPlayer);
        JoinpointLocation joinpoint = new JoinpointLocation(
            location, joinpointName, senderPlayer.getUuid(), targetTypeIsGlobal, Set.of()
        );

        database.createJoinpointAsync(joinpointName, senderPlayer.getUuid(), joinpoint).join();

        Text joinpointNameText = ECText.access(senderPlayer).accent(joinpointName);
        String messageKey = targetTypeIsGlobal ? "cmd.joinpoint.set.feedback.global"
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
        if (!database.joinpointExistsAsync(joinpointName, senderPlayer.getUuid()).join()) {
            throw new JoinpointException.Set.DeleteNotFound(joinpointName);
        }

        boolean wasSuccessful = database.deleteJoinpointAsync(joinpointName, senderPlayer.getUuid()).join();

        Text joinpointNameText = ECText.access(senderPlayer).accent(joinpointName);
        if (wasSuccessful) {
            playerData.sendCommandFeedback("cmd.joinpoint.delete.feedback", joinpointNameText);
        } else {
            throw new JoinpointException.Set.DeleteGeneric(joinpointName);
        }

        return null;
    }
}
