package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.*;
import com.fibermc.essentialcommands.util.TextUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Util;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;

public class TeleportAskHereCommand implements Command<ServerCommandSource> {

    public TeleportAskHereCommand() {}

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        TeleportRequestManager tpMgr = ManagerLocator.INSTANCE.getTpManager();
        //Store command sender
        ServerPlayerEntity senderPlayer = context.getSource().getPlayer();
        //Store Target Player
        ServerPlayerEntity targetPlayer = EntityArgumentType.getPlayer(context, "target");

        //inform target player of tp request via chat
        targetPlayer.sendSystemMessage(TextUtil.concat(
                new LiteralText(senderPlayer.getEntityName()).setStyle(CONFIG.FORMATTING_ACCENT.getValue()),
                ECText.getInstance().getText("cmd.tpaskhere.receive").setStyle(CONFIG.FORMATTING_DEFAULT.getValue())
        ), Util.NIL_UUID);

        String senderName = context.getSource().getPlayer().getGameProfile().getName();
        new ChatConfirmationPrompt(
                targetPlayer,
                "/tpaccept " + senderName,
                "/tpdeny " + senderName,
                new LiteralText("[" + ECText.getInstance().get("generic.accept") + "]").setStyle(CONFIG.FORMATTING_ACCENT.getValue()),
                new LiteralText("[" + ECText.getInstance().get("generic.deny") + "]").setStyle(CONFIG.FORMATTING_ERROR.getValue())
        ).send();

        //Mark TPRequest Sender as having requested a teleport
        tpMgr.startTpRequest(senderPlayer, targetPlayer, TeleportRequest.Type.TPA_HERE);

        //inform command sender that request has been sent
        context.getSource().sendFeedback(TextUtil.concat(
                new LiteralText("Teleport request has been sent to ").setStyle(CONFIG.FORMATTING_DEFAULT.getValue()),
                new LiteralText(targetPlayer.getEntityName()).setStyle(CONFIG.FORMATTING_ACCENT.getValue())
        ), CONFIG.BROADCAST_TO_OPS.getValue());

        return 1;
    }
}
