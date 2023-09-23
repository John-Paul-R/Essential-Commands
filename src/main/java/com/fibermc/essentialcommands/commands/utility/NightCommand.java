package com.fibermc.essentialcommands.commands.utility;

import com.fibermc.essentialcommands.playerdata.PlayerData;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ServerWorld;

public class NightCommand implements Command<ServerCommandSource> {
    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        PlayerData playerData = PlayerData.accessFromContextOrThrow(context);
        ServerWorld world = source.getServer().getOverworld();
        if (world.isNight()) {
            playerData.sendCommandFeedback("cmd.night.error.already_nighttime");
            return -1;
        }
        long time = world.getTimeOfDay();
        long timeToNight = 13000L - time % 24000L;

        world.setTimeOfDay(time + timeToNight);
        playerData.sendCommandFeedback("cmd.night.feedback");
        return 1;
    }
}
