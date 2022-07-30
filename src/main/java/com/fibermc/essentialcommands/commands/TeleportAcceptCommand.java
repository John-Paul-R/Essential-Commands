package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.PlayerData;
import com.fibermc.essentialcommands.TeleportRequest;
import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;

import com.mojang.brigadier.context.CommandContext;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class TeleportAcceptCommand extends TeleportResponseCommand {
    protected int exec(CommandContext<ServerCommandSource> context, ServerPlayerEntity senderPlayer, ServerPlayerEntity targetPlayer) {
        ServerCommandSource source = context.getSource();
        var senderPlayerData = PlayerData.access(senderPlayer);
        var targetPlayerData = ((ServerPlayerEntityAccess) targetPlayer).ec$getPlayerData();

        //identify if target player did indeed request to teleport. Continue if so, otherwise throw exception.
        TeleportRequest teleportRequest = targetPlayerData.getSentTeleportRequest();
        if (teleportRequest != null && teleportRequest.getTargetPlayer().equals(senderPlayer)) {

            //inform target player that teleport has been accepted via chat
            targetPlayerData.sendMessage("cmd.tpaccept.feedback");

            //Conduct teleportation
            teleportRequest.queue();

            //Send message to command sender confirming that request has been accepted
            senderPlayerData.sendMessage("cmd.tpaccept.feedback");

            //Clean up TPAsk
            targetPlayerData.setTpTimer(-1);
            // Remove the tp request, as it has been completed.
            teleportRequest.end();

            return 1;
        } else {
            senderPlayerData.sendError("cmd.tpa_reply.error.no_request_from_target");
            return -1;
        }

    }
}
