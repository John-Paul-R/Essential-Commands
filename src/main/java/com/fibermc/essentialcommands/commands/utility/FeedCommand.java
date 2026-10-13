package com.fibermc.essentialcommands.commands.utility;

import com.fibermc.essentialcommands.commands.CommandUtil;
import com.fibermc.essentialcommands.mixin.FoodDataAccessor;
import com.fibermc.essentialcommands.playerdata.PlayerData;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;

public class FeedCommand implements Command<CommandSourceStack> {
    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        PlayerData senderPlayerData = PlayerData.accessFromContextOrThrow(context);
        ServerPlayer targetPlayer = CommandUtil.getCommandTargetPlayer(context);

        FoodData hungerManager = targetPlayer.getFoodData();
        if (!hungerManager.needsFood()) {
            senderPlayerData.sendCommandError("cmd.feed.error.full");
            return 0;
        }

        hungerManager.setFoodLevel(20);
        hungerManager.setSaturation(5);
        ((FoodDataAccessor) hungerManager).essentialcommands$setExhaustionLevel(0);

        senderPlayerData.sendCommandFeedback("cmd.feed.feedback");

        return SINGLE_SUCCESS;
    }
}
