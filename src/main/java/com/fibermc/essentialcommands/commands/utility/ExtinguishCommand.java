package com.fibermc.essentialcommands.commands.utility;

import com.fibermc.essentialcommands.commands.CommandUtil;
import com.fibermc.essentialcommands.playerdata.PlayerData;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class ExtinguishCommand implements Command<ServerCommandSource> {
    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        PlayerData senderPlayerData = PlayerData.accessFromContextOrThrow(context);
        ServerPlayerEntity targetPlayer = CommandUtil.getCommandTargetPlayer(context);

        if (!targetPlayer.isOnFire()) {
            senderPlayerData.sendCommandError("cmd.extinguish.error.not_on_fire");
            return 0;
        }

        targetPlayer.setFireTicks(0);

        senderPlayerData.sendCommandFeedback("cmd.extinguish.feedback");

        return SINGLE_SUCCESS;
    }
}
