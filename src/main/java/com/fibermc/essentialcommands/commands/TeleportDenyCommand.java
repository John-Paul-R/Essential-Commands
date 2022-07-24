package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.ECText;
import com.fibermc.essentialcommands.PlayerData;
import com.fibermc.essentialcommands.TeleportRequest;
import com.fibermc.essentialcommands.TextFormatType;
import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;

import com.mojang.brigadier.context.CommandContext;

import net.minecraft.network.message.MessageType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;

public class TeleportDenyCommand extends TeleportResponseCommand {

    public TeleportDenyCommand() {}

    protected int exec(CommandContext<ServerCommandSource> context, ServerPlayerEntity senderPlayer, ServerPlayerEntity targetPlayer) {
        ServerCommandSource source = context.getSource();
        PlayerData targetPlayerData = ((ServerPlayerEntityAccess) targetPlayer).getEcPlayerData();

        //identify if target player did indeed request to teleport. Continue if so, otherwise throw exception.
        TeleportRequest teleportRequest = targetPlayerData.getSentTeleportRequest();
        if (teleportRequest == null || !teleportRequest.getTargetPlayer().equals(senderPlayer)) {
            source.sendError(
                ECText.getInstance().getText("cmd.tpa_reply.error.no_request_from_target", TextFormatType.Error)
            );
            return -1;
        }

        //inform target player that teleport has been accepted via chat
        targetPlayer.sendMessage(
            ECText.getInstance().getText("cmd.tpdeny.feedback"),
            MessageType.SYSTEM);

        //Send message to command sender confirming that request has been accepted
        source.sendFeedback(
            ECText.getInstance().getText("cmd.tpdeny.feedback"),
            CONFIG.BROADCAST_TO_OPS
        );

        // Clean up TPAsk
        targetPlayerData.setTpTimer(-1);
        // Remove the tp request, as it has been completed.
        teleportRequest.end();

        return 1;
    }
}
