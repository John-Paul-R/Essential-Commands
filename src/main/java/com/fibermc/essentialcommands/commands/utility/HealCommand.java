package com.fibermc.essentialcommands.commands.utility;

import com.fibermc.essentialcommands.commands.CommandUtil;
import com.fibermc.essentialcommands.playerdata.PlayerData;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

public class HealCommand implements Command<CommandSourceStack> {
    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        PlayerData senderPlayerData = PlayerData.accessFromContextOrThrow(context);
        ServerPlayer targetPlayer = CommandUtil.getCommandTargetPlayer(context);

        float currentHealth = targetPlayer.getHealth();
        float maxHealth = targetPlayer.getMaxHealth();
        if (currentHealth == maxHealth) {
            senderPlayerData.sendCommandError("cmd.heal.error.full");
            return 0;
        }

        targetPlayer.setHealth(maxHealth);

        senderPlayerData.sendCommandFeedback("cmd.heal.feedback");

        return SINGLE_SUCCESS;
    }
}
