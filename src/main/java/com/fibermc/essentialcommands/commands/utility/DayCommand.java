package com.fibermc.essentialcommands.commands.utility;

import com.fibermc.essentialcommands.playerdata.PlayerData;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;

public class DayCommand implements Command<ServerCommandSource> {
    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        PlayerData playerData = PlayerData.accessFromContextOrThrow(context);
        ServerWorld world = source.getServer().getOverworld();
        if (world.isDay()) {
            playerData.sendCommandFeedback("cmd.day.error.already_daytime");
            return -1;
        }
        long time = world.getTimeOfDay();
        long timeToDay = 24000L - time % 24000L;

        world.setTimeOfDay(time + timeToDay);
        playerData.sendCommandFeedback("cmd.day.feedback");
        return 1;
    }
}
