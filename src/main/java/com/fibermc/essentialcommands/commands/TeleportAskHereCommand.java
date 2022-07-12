package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.*;
import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.network.message.MessageType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;

public class TeleportAskHereCommand implements Command<ServerCommandSource> {

    public TeleportAskHereCommand() {}

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        TeleportRequestManager tpMgr = ManagerLocator.getInstance().getTpManager();
        ServerPlayerEntity senderPlayer = context.getSource().getPlayer();
        ServerPlayerEntity targetPlayer = EntityArgumentType.getPlayer(context, "target_player");

        // Don't allow spamming same target.
        {
            var existingTeleportRequest = ((ServerPlayerEntityAccess) senderPlayer)
                .getEcPlayerData()
                .getSentTeleportRequest();
            if (existingTeleportRequest != null && existingTeleportRequest.getTargetPlayer().equals(targetPlayer)) {
                senderPlayer.sendMessage(
                    ECText.getInstance().getText(
                        "cmd.tpask.error.exists",
                        existingTeleportRequest.getTargetPlayer().getDisplayName())
                    , MessageType.SYSTEM);
                return 0;
            }
        }

        //inform target player of tp request via chat
        targetPlayer.sendMessage(
            ECText.getInstance().getText(
                "cmd.tpaskhere.receive",
                Text.literal(senderPlayer.getEntityName()).setStyle(CONFIG.FORMATTING_ACCENT)),
            MessageType.SYSTEM);

        String senderName = context.getSource().getPlayer().getGameProfile().getName();
        new ChatConfirmationPrompt(
            targetPlayer,
            "/tpaccept " + senderName,
            "/tpdeny " + senderName,
            Text.literal("[" + ECText.getInstance().get("generic.accept") + "]").setStyle(CONFIG.FORMATTING_ACCENT),
            Text.literal("[" + ECText.getInstance().get("generic.deny") + "]").setStyle(CONFIG.FORMATTING_ERROR)
        ).send();

        //Mark TPRequest Sender as having requested a teleport
        tpMgr.startTpRequest(senderPlayer, targetPlayer, TeleportRequest.Type.TPA_HERE);

        var targetPlayerText = Text.literal(targetPlayer.getEntityName()).setStyle(CONFIG.FORMATTING_ACCENT);
        //inform command sender that request has been sent
        context.getSource().sendFeedback(
            ECText.getInstance().getText("cmd.tpask.send", targetPlayerText),
            CONFIG.BROADCAST_TO_OPS);

        return 1;
    }
}
