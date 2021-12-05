package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.ECText;
import com.fibermc.essentialcommands.PlayerData;
import com.fibermc.essentialcommands.TeleportRequest;
import com.fibermc.essentialcommands.TeleportRequestManager;
import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Util;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;

public class TeleportDenyCommand extends TeleportResponseCommand {

    public TeleportDenyCommand() {}

    protected int exec(CommandContext<ServerCommandSource> context, ServerPlayerEntity senderPlayer, ServerPlayerEntity targetPlayer) {
        ServerCommandSource source = context.getSource();
        PlayerData targetPlayerData = ((ServerPlayerEntityAccess)targetPlayer).getEcPlayerData();

        //identify if target player did indeed request to teleport. Continue if so, otherwise throw exception.
        TeleportRequest teleportRequest = targetPlayerData.getSentTeleportRequest();
        if (teleportRequest != null && teleportRequest.getTargetPlayer().equals(senderPlayer)) {
            //inform target player that teleport has been accepted via chat
            targetPlayer.sendSystemMessage(
                    ECText.getInstance().getText("cmd.tpdeny.feedback").setStyle(CONFIG.FORMATTING_DEFAULT.getValue())
                    , Util.NIL_UUID);

            //Send message to command sender confirming that request has been accepted
            source.sendFeedback(
                    ECText.getInstance().getText("cmd.tpdeny.feedback").setStyle(CONFIG.FORMATTING_DEFAULT.getValue())
                    , CONFIG.BROADCAST_TO_OPS.getValue()
            );

            //Clean up TPAsk
            targetPlayerData.setTpTimer(-1);
            // Remove the tp request, as it has been completed.
            teleportRequest.end();

            return 1;
        } else {
            source.sendError(
                    ECText.getInstance().getText("cmd.tpa_reply.error.no_request_from_target").setStyle(CONFIG.FORMATTING_ERROR.getValue())
            );
            return -1;
        }

    }
}
