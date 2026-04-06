package com.fibermc.essentialcommands.commands.utility;

import com.fibermc.essentialcommands.playerdata.PlayerData;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.ServerLevelData;

public class NightCommand implements Command<CommandSourceStack> {
    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        PlayerData playerData = PlayerData.accessFromContextOrThrow(context);
        ServerLevel world = source.getServer().overworld();
        if (world.isDarkOutside()) {
            playerData.sendCommandFeedback("cmd.night.error.already_nighttime");
            return -1;
        }
        long time = world.getGameTime();
        long timeToNight = 13000L - time % 24000L;

        ((ServerLevelData)world.getLevelData()).setGameTime(time + timeToNight);

        playerData.sendCommandFeedback("cmd.night.feedback");
        return 1;
    }
}
