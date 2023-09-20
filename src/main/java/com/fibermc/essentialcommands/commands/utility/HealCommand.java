package com.fibermc.essentialcommands.commands.utility;

import com.fibermc.essentialcommands.commands.CommandUtil;
import com.fibermc.essentialcommands.playerdata.PlayerData;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class HealCommand implements Command<ServerCommandSource> {
    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        PlayerData senderPlayerData = PlayerData.accessFromContextOrThrow(context);
        ServerPlayerEntity targetPlayer = CommandUtil.getCommandTargetPlayer(context);

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
