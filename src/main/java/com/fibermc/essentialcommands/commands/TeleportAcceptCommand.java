package com.fibermc.essentialcommands.commands;

import java.util.Optional;

import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.playerdata.PlayerData;
import com.fibermc.essentialcommands.teleportation.TeleportRequest;

import com.mojang.brigadier.context.CommandContext;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class TeleportAcceptCommand extends TeleportResponseCommand {
    protected int exec(CommandContext<ServerCommandSource> context, ServerPlayerEntity respondingPlayer, ServerPlayerEntity requesterPlayer) {
        var senderPlayerData = PlayerData.access(respondingPlayer);
        var targetPlayerData = ((ServerPlayerEntityAccess) requesterPlayer).ec$getPlayerData();

        //identify if target player did indeed request to teleport. Continue if so, otherwise throw exception.
        Optional<TeleportRequest> teleportRequest = targetPlayerData.getSentTeleportRequests()
            .getRequestToPlayer(senderPlayerData);
        if (teleportRequest.isPresent() && teleportRequest.get().getTargetPlayer().equals(respondingPlayer)) {

            //inform target player that teleport has been accepted via chat
            targetPlayerData.sendMessage("cmd.tpaccept.feedback");

            //Conduct teleportation
            teleportRequest.get().queue();

            //Send message to command sender confirming that request has been accepted
            senderPlayerData.sendMessage("cmd.tpaccept.feedback");

            // Remove the tp request, as it has been completed.
            teleportRequest.get().end();

            return SINGLE_SUCCESS;
        } else {
            senderPlayerData.sendError("cmd.tpa_reply.error.no_request_from_target");
            return -1;
        }

    }
}
