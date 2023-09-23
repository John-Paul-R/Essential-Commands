package com.fibermc.essentialcommands.commands.utility;

import com.fibermc.essentialcommands.commands.CommandUtil;
import com.fibermc.essentialcommands.playerdata.PlayerData;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.entity.player.HungerManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class FeedCommand implements Command<ServerCommandSource> {
    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        PlayerData senderPlayerData = PlayerData.accessFromContextOrThrow(context);
        ServerPlayerEntity targetPlayer = CommandUtil.getCommandTargetPlayer(context);

        HungerManager hungerManager = targetPlayer.getHungerManager();
        if (!hungerManager.isNotFull()) {
            senderPlayerData.sendCommandError("cmd.feed.error.full");
            return 0;
        }

        hungerManager.setFoodLevel(20);
        hungerManager.setSaturationLevel(5);
        hungerManager.setExhaustion(0);

        senderPlayerData.sendCommandFeedback("cmd.feed.feedback");

        return SINGLE_SUCCESS;
    }
}
