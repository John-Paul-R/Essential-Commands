package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.playerdata.PlayerData;
import com.fibermc.essentialcommands.teleportation.TeleportRequest;

import com.mojang.brigadier.context.CommandContext;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class TeleportDenyCommand extends TeleportResponseCommand {
    protected int exec(CommandContext<ServerCommandSource> context, ServerPlayerEntity senderPlayer, ServerPlayerEntity targetPlayer) {
        var targetPlayerData = PlayerData.access(targetPlayer);
        var senderPlayerData = PlayerData.access(senderPlayer);

        //identify if target player did indeed request to teleport. Continue if so, otherwise throw exception.
        TeleportRequest teleportRequest = targetPlayerData.getSentTeleportRequest();
        if (teleportRequest == null || !teleportRequest.getTargetPlayer().equals(senderPlayer)) {
            senderPlayerData.sendCommandError("cmd.tpa_reply.error.no_request_from_target");
            return -1;
        }

        //inform target player that teleport has been accepted via chat
        targetPlayerData.sendMessage("cmd.tpdeny.feedback");

        //Send message to command sender confirming that request has been accepted
        senderPlayerData.sendCommandFeedback("cmd.tpdeny.feedback");

        // Remove the tp request, as it has been completed.
        teleportRequest.end();

        return 1;
    }
}
