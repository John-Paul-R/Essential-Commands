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

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

public abstract class TeleportResponseCommand implements Command<CommandSourceStack> {

    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return exec(
            context,
            context.getSource().getPlayer(),
            NicknameTargetResolver.getPlayer(context, "target_player")
        );
    }

    public int runDefault(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var respondingPlayer = context.getSource().getPlayerOrException();
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

        ServerPlayer teleportRequestSender = incomingTeleportRequests.values().stream().findFirst().get().getSenderPlayer();
        if (teleportRequestSender == null) {
            throw CommandUtil.createSimpleException(
                ecText.getText("cmd.tpa_reply.error.no_request_from_target", TextFormatType.Error));
        }

        return exec(context, respondingPlayer, teleportRequestSender);
    }

    abstract int exec(CommandContext<CommandSourceStack> context, ServerPlayer respondingPlayer, ServerPlayer requesterPlayer);

}
