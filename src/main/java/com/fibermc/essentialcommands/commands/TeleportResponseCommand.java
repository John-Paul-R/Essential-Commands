package com.fibermc.essentialcommands.commands;

import java.util.LinkedHashMap;
import java.util.UUID;

import com.fibermc.essentialcommands.ECText;
import com.fibermc.essentialcommands.PlayerData;
import com.fibermc.essentialcommands.TeleportRequest;
import com.fibermc.essentialcommands.TextFormatType;
import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public abstract class TeleportResponseCommand implements Command<ServerCommandSource> {

    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return exec(
            context,
            context.getSource().getPlayer(),
            EntityArgumentType.getPlayer(context, "target_player")
        );
    }

    public int runDefault(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity senderPlayer = context.getSource().getPlayer();
        PlayerData senderPlayerData = ((ServerPlayerEntityAccess) senderPlayer).ec$getPlayerData();
        LinkedHashMap<UUID, TeleportRequest> incomingTeleportRequests = senderPlayerData.getIncomingTeleportRequests();

        if (incomingTeleportRequests.size() > 1) {
            throw CommandUtil.createSimpleException(
                ECText.getInstance().getText("cmd.tpa_reply.error.shortcut_more_than_one", TextFormatType.Error));
        } else if (incomingTeleportRequests.size() < 1) {
            throw CommandUtil.createSimpleException(
                ECText.getInstance().getText("cmd.tpa_reply.error.shortcut_none_exist", TextFormatType.Error));
        }

        ServerPlayerEntity targetPlayer = incomingTeleportRequests.values().stream().findFirst().get().getTargetPlayer();
        if (targetPlayer == null) {
            throw CommandUtil.createSimpleException(
                ECText.getInstance().getText("cmd.tpa_reply.error.no_request_from_target", TextFormatType.Error));
        }

        return exec(
            context,
            senderPlayer,
            targetPlayer
        );
    }

    abstract int exec(CommandContext<ServerCommandSource> context, ServerPlayerEntity senderPlayer, ServerPlayerEntity targetPlayer);

}
