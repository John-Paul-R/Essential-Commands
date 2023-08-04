package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.playerdata.PlayerData;
import com.fibermc.essentialcommands.teleportation.TeleportRequest;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import dev.jpcode.eccore.util.TextUtil;

public class TeleportCancelCommand implements Command<ServerCommandSource> {

    public TeleportCancelCommand() {}

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        //Store command sender
        ServerPlayerEntity senderPlayer = context.getSource().getPlayerOrThrow();
        var senderPlayerData = PlayerData.access(senderPlayer);

        var existingTeleportRequests = senderPlayerData.getSentTeleportRequests();

        if (existingTeleportRequests.size() == 0) {
            senderPlayerData.sendCommandError("cmd.tpcancel.error.no_exists");
            return 0;
        }

        var targetPlayers = existingTeleportRequests.stream().map(TeleportRequest::getTargetPlayerData).toList();
        existingTeleportRequests.clear();

        senderPlayerData.sendCommandFeedback(
            "cmd.tpcancel.feedback",
            TextUtil.join(
                targetPlayers.stream().map(PlayerData::getPlayer).map(ServerPlayerEntity::getDisplayName).toList(),
                Text.literal(", "))
        );

        return SINGLE_SUCCESS;
    }
}
