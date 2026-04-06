package com.fibermc.essentialcommands.commands.utility;

import com.fibermc.essentialcommands.playerdata.PlayerData;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.ServerLevelData;

public class DayCommand implements Command<CommandSourceStack> {
    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        PlayerData playerData = PlayerData.accessFromContextOrThrow(context);
        ServerLevel world = source.getServer().overworld();
        if (world.isBrightOutside()) {
            playerData.sendCommandFeedback("cmd.day.error.already_daytime");
            return -1;
        }
        long time = world.getGameTime();
        long timeToDay = 24000L - time % 24000L;

        ((ServerLevelData)world.getLevelData()).setGameTime(time + timeToDay);

        playerData.sendCommandFeedback("cmd.day.feedback");
        return 1;
    }
}
