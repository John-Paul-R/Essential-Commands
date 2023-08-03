package com.fibermc.essentialcommands.commands;

import java.util.LinkedHashMap;
import java.util.UUID;

import com.fibermc.essentialcommands.playerdata.PlayerData;
import com.fibermc.essentialcommands.teleportation.TeleportRequest;
import com.fibermc.essentialcommands.text.ECText;
import com.fibermc.essentialcommands.text.TextFormatType;

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
        var respondingPlayer = context.getSource().getPlayerOrThrow();
        var respondingPlayerData = PlayerData.access(respondingPlayer);
        var ecText = ECText.access(respondingPlayer);
        LinkedHashMap<UUID, TeleportRequest> incomingTeleportRequests = respondingPlayerData.getIncomingTeleportRequests();

        if (incomingTeleportRequests.size() > 1) {
            throw CommandUtil.createSimpleException(
                ecText.getText("cmd.tpa_reply.error.shortcut_more_than_one", TextFormatType.Error));
        } else if (incomingTeleportRequests.size() < 1) {
            throw CommandUtil.createSimpleException(
                ecText.getText("cmd.tpa_reply.error.shortcut_none_exist", TextFormatType.Error));
        }

        ServerPlayerEntity teleportRequestSender = incomingTeleportRequests.values().stream().findFirst().get().getSenderPlayer();
        if (teleportRequestSender == null) {
            throw CommandUtil.createSimpleException(
                ecText.getText("cmd.tpa_reply.error.no_request_from_target", TextFormatType.Error));
        }

        return exec(context, respondingPlayer, teleportRequestSender);
    }

    abstract int exec(CommandContext<ServerCommandSource> context, ServerPlayerEntity respondingPlayer, ServerPlayerEntity requesterPlayer);

}
