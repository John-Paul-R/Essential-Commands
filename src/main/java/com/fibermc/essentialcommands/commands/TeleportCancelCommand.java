package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.playerdata.PlayerData;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class TeleportCancelCommand implements Command<ServerCommandSource> {

    public TeleportCancelCommand() {}

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        //Store command sender
        ServerPlayerEntity senderPlayer = context.getSource().getPlayer();
        var senderPlayerData = PlayerData.access(senderPlayer);

        var existingTeleportRequest = senderPlayerData.getSentTeleportRequest();

        if (existingTeleportRequest == null) {
            senderPlayerData.sendCommandError("cmd.tpcancel.error.no_exists");
            return 0;
        }

        existingTeleportRequest.end();

        senderPlayerData.sendCommandFeedback(
            "cmd.tpcancel.feedback",
            existingTeleportRequest.getTargetPlayer().getDisplayName()
        );

        return 1;
    }
}
