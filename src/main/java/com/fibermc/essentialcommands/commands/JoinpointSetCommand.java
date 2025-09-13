package com.fibermc.essentialcommands.commands;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.fibermc.essentialcommands.ManagerLocator;
import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.database.JoinpointDatabase;
import com.fibermc.essentialcommands.playerdata.PlayerData;
import com.fibermc.essentialcommands.text.ChatConfirmationPrompt;
import com.fibermc.essentialcommands.text.ECText;
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

    private int exec(CommandContext<ServerCommandSource> context, String joinpointName) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity senderPlayer = source.getPlayerOrThrow();
        PlayerData playerData = ((ServerPlayerEntityAccess) senderPlayer).ec$getPlayerData();

        JoinpointDatabase database = ManagerLocator.getInstance().getJoinpointDatabase();

        try {
            switch (action) {
                case SET -> handleSet(context, joinpointName, senderPlayer, playerData, database);
                case OVERWRITE -> handleOverwrite(context, joinpointName, senderPlayer, playerData, database);
                case DELETE -> handleDelete(context, joinpointName, senderPlayer, playerData, database);
            }
        } catch (SQLException e) {
            playerData.sendCommandError("cmd.joinpoint.error.database", Text.literal(e.getMessage()));
            return 0;
        }

        return SINGLE_SUCCESS;
    }

    private void handleSet(CommandContext<ServerCommandSource> context, String joinpointName,
                          ServerPlayerEntity senderPlayer, PlayerData playerData, JoinpointDatabase database)
                          throws CommandSyntaxException, SQLException {

        if (database.joinpointExists(joinpointName, senderPlayer.getUuid())) {
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
        } else {
            // Create new joinpoint
            boolean isGlobal = false;
            Set<UUID> sharedWith = new HashSet<>();

            // Check if global flag is provided
            try {
                isGlobal = BoolArgumentType.getBool(context, "global");
            } catch (IllegalArgumentException ignored) {
                // Optional parameter not provided
            }

            // Note: Sharing is now handled by /joinpoint share command

            MinecraftLocation location = new MinecraftLocation(senderPlayer);
            JoinpointLocation joinpoint = new JoinpointLocation(
                location, joinpointName, senderPlayer.getUuid(), isGlobal, sharedWith
            );

            database.createJoinpoint(joinpointName, senderPlayer.getUuid(), joinpoint);

            Text joinpointNameText = ECText.access(senderPlayer).accent(joinpointName);
            String messageKey = isGlobal ? "cmd.joinpoint.set.feedback.global" :
                               !sharedWith.isEmpty() ? "cmd.joinpoint.set.feedback.shared" :
                               "cmd.joinpoint.set.feedback";

            playerData.sendCommandFeedback(messageKey, joinpointNameText);
        }
    }

    private void handleOverwrite(CommandContext<ServerCommandSource> context, String joinpointName,
                               ServerPlayerEntity senderPlayer, PlayerData playerData, JoinpointDatabase database)
        throws SQLException
    {

        boolean isGlobal = false;
        Set<UUID> sharedWith = new HashSet<>();

        // Check if global flag is provided
        try {
            isGlobal = BoolArgumentType.getBool(context, "global");
        } catch (IllegalArgumentException ignored) {
            // Optional parameter not provided
        }

        // Note: Sharing is now handled by /joinpoint share command

        MinecraftLocation location = new MinecraftLocation(senderPlayer);
        JoinpointLocation joinpoint = new JoinpointLocation(
            location, joinpointName, senderPlayer.getUuid(), isGlobal, sharedWith
        );

        if (database.joinpointExists(joinpointName, senderPlayer.getUuid())) {
            database.updateJoinpoint(joinpointName, senderPlayer.getUuid(), joinpoint);
        } else {
            database.createJoinpoint(joinpointName, senderPlayer.getUuid(), joinpoint);
        }

        Text joinpointNameText = ECText.access(senderPlayer).accent(joinpointName);
        String messageKey = isGlobal ? "cmd.joinpoint.overwrite.feedback.global" :
                           !sharedWith.isEmpty() ? "cmd.joinpoint.overwrite.feedback.shared" :
                           "cmd.joinpoint.overwrite.feedback";

        playerData.sendCommandFeedback(messageKey, joinpointNameText);
    }

    private void handleDelete(CommandContext<ServerCommandSource> context, String joinpointName,
                            ServerPlayerEntity senderPlayer, PlayerData playerData, JoinpointDatabase database)
                            throws SQLException {

        boolean wasSuccessful = database.deleteJoinpoint(joinpointName, senderPlayer.getUuid());

        Text joinpointNameText = ECText.access(senderPlayer).accent(joinpointName);
        if (wasSuccessful) {
            playerData.sendCommandFeedback("cmd.joinpoint.delete.feedback", joinpointNameText);
        } else {
            playerData.sendCommandError("cmd.joinpoint.delete.error", joinpointNameText);
        }
    }
}
