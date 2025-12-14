package com.fibermc.essentialcommands.commands.utility;

import com.fibermc.essentialcommands.commands.CommandUtil;
import com.fibermc.essentialcommands.playerdata.PlayerData;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

public class ExtinguishCommand implements Command<CommandSourceStack> {
    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        PlayerData senderPlayerData = PlayerData.accessFromContextOrThrow(context);
        ServerPlayer targetPlayer = CommandUtil.getCommandTargetPlayer(context);

        if (!targetPlayer.isOnFire()) {
            senderPlayerData.sendCommandError("cmd.extinguish.error.not_on_fire");
            return 0;
        }

        targetPlayer.setRemainingFireTicks(0);

        senderPlayerData.sendCommandFeedback("cmd.extinguish.feedback");

        return SINGLE_SUCCESS;
    }
}
