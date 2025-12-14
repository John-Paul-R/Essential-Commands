package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.playerdata.PlayerData;
import com.fibermc.essentialcommands.teleportation.TeleportRequest;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import dev.jpcode.eccore.util.TextUtil;

public class TeleportCancelCommand implements Command<CommandSourceStack> {

    public TeleportCancelCommand() {}

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        //Store command sender
        ServerPlayer senderPlayer = context.getSource().getPlayerOrException();
        var senderPlayerData = PlayerData.access(senderPlayer);

        var existingTeleportRequests = senderPlayerData.getSentTeleportRequests();

        if (existingTeleportRequests.size() == 0) {
            senderPlayerData.sendCommandError("cmd.tpcancel.error.no_exists");
            return 0;
        }

        var targetPlayers = existingTeleportRequests.stream().map(TeleportRequest::getTargetPlayerData).toList();
        existingTeleportRequests.clear();

        senderPlayerData.sendCommandFeedback(
            "cmd.tpcancel.feedback",
            TextUtil.join(
                targetPlayers.stream().map(PlayerData::getPlayer).map(ServerPlayer::getDisplayName).toList(),
                Component.literal(", "))
        );

        return SINGLE_SUCCESS;
    }
}
