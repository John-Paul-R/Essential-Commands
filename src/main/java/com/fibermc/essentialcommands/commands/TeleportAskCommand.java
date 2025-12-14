package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.ManagerLocator;
import com.fibermc.essentialcommands.playerdata.PlayerData;
import com.fibermc.essentialcommands.playerdata.PlayerProfile;
import com.fibermc.essentialcommands.teleportation.TeleportManager;
import com.fibermc.essentialcommands.teleportation.TeleportRequest;
import com.fibermc.essentialcommands.text.ChatConfirmationPrompt;
import com.fibermc.essentialcommands.text.ECText;
import com.fibermc.essentialcommands.text.TextFormatType;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

public class TeleportAskCommand implements Command<CommandSourceStack> {

    public TeleportAskCommand() {}

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        TeleportManager tpMgr = ManagerLocator.getInstance().getTpManager();
        ServerPlayer senderPlayer = context.getSource().getPlayerOrException();
        ServerPlayer targetPlayer = EntityArgument.getPlayer(context, "target_player");
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
        var targetPlayerProfile = PlayerProfile.access(targetPlayer);
        targetPlayerData.sendMessage(
            "cmd.tpask.receive",
            senderPlayer.getDisplayName().copy().withStyle(targetPlayerProfile.getStyle(TextFormatType.Accent))
        );

        String senderName = senderPlayer.getGameProfile().name();
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
        var senderPlayerProfile = PlayerProfile.access(senderPlayer);
        var targetPlayerText = targetPlayer.getDisplayName().copy().withStyle(senderPlayerProfile.getStyle(TextFormatType.Accent));
        senderPlayerData.sendCommandFeedback("cmd.tpask.send", targetPlayerText);

        return SINGLE_SUCCESS;
    }
}
