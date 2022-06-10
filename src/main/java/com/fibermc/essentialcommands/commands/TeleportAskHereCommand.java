package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.*;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.jpcode.eccore.util.TextUtil;
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

        //inform target player of tp request via chat
        targetPlayer.sendMessage(TextUtil.concat(
            Text.literal(senderPlayer.getEntityName()).setStyle(CONFIG.FORMATTING_ACCENT.getValue()),
            ECText.getInstance().getText("cmd.tpaskhere.receive").setStyle(CONFIG.FORMATTING_DEFAULT.getValue())
        ), MessageType.SYSTEM);

        String senderName = context.getSource().getPlayer().getGameProfile().getName();
        new ChatConfirmationPrompt(
            targetPlayer,
            "/tpaccept " + senderName,
            "/tpdeny " + senderName,
            Text.literal("[" + ECText.getInstance().get("generic.accept") + "]").setStyle(CONFIG.FORMATTING_ACCENT.getValue()),
            Text.literal("[" + ECText.getInstance().get("generic.deny") + "]").setStyle(CONFIG.FORMATTING_ERROR.getValue())
        ).send();

        //Mark TPRequest Sender as having requested a teleport
        tpMgr.startTpRequest(senderPlayer, targetPlayer, TeleportRequest.Type.TPA_HERE);

        //inform command sender that request has been sent
        context.getSource().sendFeedback(TextUtil.concat(
            Text.literal("Teleport request has been sent to ").setStyle(CONFIG.FORMATTING_DEFAULT.getValue()),
            Text.literal(targetPlayer.getEntityName()).setStyle(CONFIG.FORMATTING_ACCENT.getValue())
        ), CONFIG.BROADCAST_TO_OPS.getValue());

        return 1;
    }
}
