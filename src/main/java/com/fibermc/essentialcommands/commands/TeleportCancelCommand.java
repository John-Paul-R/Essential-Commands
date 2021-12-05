package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.*;
import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
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

public class TeleportCancelCommand implements Command<ServerCommandSource> {

    public TeleportCancelCommand() {}

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        //Store command sender
        ServerPlayerEntity senderPlayer = context.getSource().getPlayer();

        var existingTeleportRequest = ((ServerPlayerEntityAccess) senderPlayer)
            .getEcPlayerData()
            .getSentTeleportRequest();

        if (existingTeleportRequest == null) {
            senderPlayer.sendSystemMessage(
                ECText.getInstance().getText("cmd.tpcancel.error.no_exists"),
                Util.NIL_UUID);
            return 0;
        }

        existingTeleportRequest.end();

        senderPlayer.sendSystemMessage(
            TextUtil.concat(
                ECText.getInstance().getText("cmd.tpcancel.feedback.1"),
                existingTeleportRequest.getTargetPlayer().getDisplayName(),
                ECText.getInstance().getText("cmd.tpcancel.feedback.2")),
            Util.NIL_UUID);

        return 1;
    }
}
