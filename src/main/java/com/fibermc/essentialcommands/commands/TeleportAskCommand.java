package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.ManagerLocator;
import com.fibermc.essentialcommands.playerdata.PlayerData;
import com.fibermc.essentialcommands.teleportation.TeleportManager;
import com.fibermc.essentialcommands.teleportation.TeleportRequest;
import com.fibermc.essentialcommands.text.ChatConfirmationPrompt;
import com.fibermc.essentialcommands.text.ECText;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class TeleportAskCommand implements Command<ServerCommandSource> {

    public TeleportAskCommand() {}

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        TeleportManager tpMgr = ManagerLocator.getInstance().getTpManager();
        ServerPlayerEntity senderPlayer = context.getSource().getPlayerOrThrow();
        ServerPlayerEntity targetPlayer = EntityArgumentType.getPlayer(context, "target_player");
        var senderPlayerData = PlayerData.access(senderPlayer);
        var targetPlayerData = PlayerData.access(targetPlayer);

        // Don't allow spamming same target.
        {
            var existingTeleportRequest = senderPlayerData.getSentTeleportRequests()
                .getRequestToPlayer(targetPlayerData);
            if (existingTeleportRequest.isPresent()) {
                PlayerData.access(senderPlayer).sendCommandError(
                    "cmd.tpask.error.exists",
                    existingTeleportRequest.get().getTargetPlayer().getDisplayName());
                return 0;
            }
        }

        //inform target player of tp request via chat
        var targetPlayerEcText = ECText.access(targetPlayer);
        targetPlayerData.sendMessage(
            "cmd.tpask.receive",
            targetPlayerEcText.accent(senderPlayer.getNameForScoreboard())
        );

        String senderName = senderPlayer.getGameProfile().getName();
        new ChatConfirmationPrompt(
            targetPlayer,
            "/tpaccept " + senderName,
            "/tpdeny " + senderName,
            targetPlayerEcText.accent("[" + ECText.getInstance().getString("generic.accept") + "]"),
            targetPlayerEcText.error("[" + ECText.getInstance().getString("generic.deny") + "]")
        ).send();

        //Mark TPRequest Sender as having requested a teleport
        tpMgr.startTpRequest(senderPlayer, targetPlayer, TeleportRequest.Type.TPA_TO);

        //inform command sender that request has been sent
        var targetPlayerText = ECText.access(senderPlayer).accent(targetPlayer.getNameForScoreboard());
        senderPlayerData.sendCommandFeedback("cmd.tpask.send", targetPlayerText);

        return SINGLE_SUCCESS;
    }
}
