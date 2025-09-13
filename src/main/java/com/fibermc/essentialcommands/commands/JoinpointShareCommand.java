package com.fibermc.essentialcommands.commands;

import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.fibermc.essentialcommands.ManagerLocator;
import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.database.JoinpointDatabase;
import com.fibermc.essentialcommands.playerdata.PlayerData;
import com.fibermc.essentialcommands.text.ECText;
import com.fibermc.essentialcommands.text.TextFormatType;
import com.fibermc.essentialcommands.types.JoinpointLocation;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

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
    
    private int exec(CommandContext<ServerCommandSource> context, String joinpointName) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity senderPlayer = source.getPlayerOrThrow();
        PlayerData playerData = ((ServerPlayerEntityAccess) senderPlayer).ec$getPlayerData();
        
        JoinpointDatabase database = ManagerLocator.getInstance().getJoinpointDatabase();
        
        try {
            switch (action) {
                case ADD -> handleAdd(context, joinpointName, senderPlayer, playerData, database);
                case REMOVE -> handleRemove(context, joinpointName, senderPlayer, playerData, database);
                case LIST -> handleList(context, joinpointName, senderPlayer, playerData, database);
                case CLEAR -> handleClear(context, joinpointName, senderPlayer, playerData, database);
            }
        } catch (SQLException e) {
            playerData.sendCommandError("cmd.joinpoint.error.database", Text.literal(e.getMessage()));
            return 0;
        }
        
        return SINGLE_SUCCESS;
    }
    
    private void handleAdd(CommandContext<ServerCommandSource> context, String joinpointName,
                          ServerPlayerEntity senderPlayer, PlayerData playerData, JoinpointDatabase database)
                          throws CommandSyntaxException, SQLException {
        
        // Get the joinpoint
        JoinpointLocation joinpoint = database.getJoinpoint(joinpointName, senderPlayer.getUuid());
        if (joinpoint == null) {
            Message msg = ECText.access(senderPlayer).getText(
                "cmd.joinpoint.share.error.not_found",
                TextFormatType.Error,
                Text.literal(joinpointName)
            );
            throw new CommandSyntaxException(new SimpleCommandExceptionType(msg), msg);
        }
        
        // Check if it's already global
        if (joinpoint.isGlobal()) {
            playerData.sendCommandError("cmd.joinpoint.share.error.already_global", 
                ECText.access(senderPlayer).accent(joinpointName));
            return;
        }
        
        // Get the players to share with
        var targetPlayers = EntityArgumentType.getPlayers(context, "target_players");
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
            playerData.sendCommandError("cmd.joinpoint.share.error.no_new_players");
            return;
        }
        
        // Create updated joinpoint
        JoinpointLocation updatedJoinpoint = new JoinpointLocation(
            joinpoint, joinpoint.getName(), joinpoint.getOwner(), false, newSharedWith
        );
        
        database.updateJoinpoint(joinpointName, senderPlayer.getUuid(), updatedJoinpoint);
        
        Text joinpointNameText = ECText.access(senderPlayer).accent(joinpointName);
        Text playersText = Text.literal(String.join(", ", addedPlayerNames));
        playerData.sendCommandFeedback("cmd.joinpoint.share.add.feedback", joinpointNameText, playersText);
    }
    
    private void handleRemove(CommandContext<ServerCommandSource> context, String joinpointName,
                             ServerPlayerEntity senderPlayer, PlayerData playerData, JoinpointDatabase database)
                             throws CommandSyntaxException, SQLException {
        
        // Get the joinpoint
        JoinpointLocation joinpoint = database.getJoinpoint(joinpointName, senderPlayer.getUuid());
        if (joinpoint == null) {
            Message msg = ECText.access(senderPlayer).getText(
                "cmd.joinpoint.share.error.not_found",
                TextFormatType.Error,
                Text.literal(joinpointName)
            );
            throw new CommandSyntaxException(new SimpleCommandExceptionType(msg), msg);
        }
        
        // Get the players to remove sharing from
        var targetPlayers = EntityArgumentType.getPlayers(context, "target_players");
        Set<UUID> newSharedWith = new HashSet<>(joinpoint.getSharedWith());
        Set<String> removedPlayerNames = new HashSet<>();
        
        for (var targetPlayer : targetPlayers) {
            if (newSharedWith.remove(targetPlayer.getUuid())) {
                removedPlayerNames.add(targetPlayer.getName().getString());
            }
        }
        
        if (removedPlayerNames.isEmpty()) {
            playerData.sendCommandError("cmd.joinpoint.share.error.players_not_shared");
            return;
        }
        
        // Create updated joinpoint
        JoinpointLocation updatedJoinpoint = new JoinpointLocation(
            joinpoint, joinpoint.getName(), joinpoint.getOwner(), joinpoint.isGlobal(), newSharedWith
        );
        
        database.updateJoinpoint(joinpointName, senderPlayer.getUuid(), updatedJoinpoint);
        
        Text joinpointNameText = ECText.access(senderPlayer).accent(joinpointName);
        Text playersText = Text.literal(String.join(", ", removedPlayerNames));
        playerData.sendCommandFeedback("cmd.joinpoint.share.remove.feedback", joinpointNameText, playersText);
    }
    
    private void handleList(CommandContext<ServerCommandSource> context, String joinpointName,
                           ServerPlayerEntity senderPlayer, PlayerData playerData, JoinpointDatabase database)
                           throws CommandSyntaxException, SQLException {
        
        // Get the joinpoint
        JoinpointLocation joinpoint = database.getJoinpoint(joinpointName, senderPlayer.getUuid());
        if (joinpoint == null) {
            Message msg = ECText.access(senderPlayer).getText(
                "cmd.joinpoint.share.error.not_found",
                TextFormatType.Error,
                Text.literal(joinpointName)
            );
            throw new CommandSyntaxException(new SimpleCommandExceptionType(msg), msg);
        }
        
        Text joinpointNameText = ECText.access(senderPlayer).accent(joinpointName);
        
        if (joinpoint.isGlobal()) {
            playerData.sendCommandFeedback("cmd.joinpoint.share.list.global", joinpointNameText);
            return;
        }
        
        if (joinpoint.getSharedWith().isEmpty()) {
            playerData.sendCommandFeedback("cmd.joinpoint.share.list.private", joinpointNameText);
            return;
        }
        
        // Get player names using cached names for performance
        Set<String> sharedPlayerNames = new HashSet<>();
        var cachedNames = database.getCachedNamesForUuids(joinpoint.getSharedWith());
        
        for (UUID uuid : joinpoint.getSharedWith()) {
            String name = cachedNames.get(uuid);
            if (name != null) {
                sharedPlayerNames.add(name);
            } else {
                sharedPlayerNames.add(uuid.toString().substring(0, 8) + "..."); // Fallback to truncated UUID
            }
        }
        
        Text playersText = Text.literal(String.join(", ", sharedPlayerNames));
        playerData.sendCommandFeedback("cmd.joinpoint.share.list.shared", joinpointNameText, playersText);
    }
    
    private void handleClear(CommandContext<ServerCommandSource> context, String joinpointName,
                            ServerPlayerEntity senderPlayer, PlayerData playerData, JoinpointDatabase database)
                            throws CommandSyntaxException, SQLException {
        
        // Get the joinpoint
        JoinpointLocation joinpoint = database.getJoinpoint(joinpointName, senderPlayer.getUuid());
        if (joinpoint == null) {
            Message msg = ECText.access(senderPlayer).getText(
                "cmd.joinpoint.share.error.not_found",
                TextFormatType.Error,
                Text.literal(joinpointName)
            );
            throw new CommandSyntaxException(new SimpleCommandExceptionType(msg), msg);
        }
        
        // Check if it's global (can't clear global status this way)
        if (joinpoint.isGlobal()) {
            playerData.sendCommandError("cmd.joinpoint.share.error.cannot_clear_global", 
                ECText.access(senderPlayer).accent(joinpointName));
            return;
        }
        
        // Clear all sharing
        JoinpointLocation updatedJoinpoint = new JoinpointLocation(
            joinpoint, joinpoint.getName(), joinpoint.getOwner(), false, new HashSet<>()
        );
        
        database.updateJoinpoint(joinpointName, senderPlayer.getUuid(), updatedJoinpoint);
        
        Text joinpointNameText = ECText.access(senderPlayer).accent(joinpointName);
        playerData.sendCommandFeedback("cmd.joinpoint.share.clear.feedback", joinpointNameText);
    }
}