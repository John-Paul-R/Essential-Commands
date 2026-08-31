package com.fibermc.essentialcommands.commands.utility;

import com.fibermc.essentialcommands.playerdata.PlayerData;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.clock.ClockTimeMarkers;
import net.minecraft.world.clock.WorldClock;

import java.util.Optional;

public class NightCommand implements Command<CommandSourceStack> {
    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        PlayerData playerData = PlayerData.accessFromContextOrThrow(context);
        ServerLevel level = source.getPlayer().level();

        Optional<Holder<WorldClock>> clock = level.dimensionType().defaultClock();
        if (clock.isEmpty()) {
            playerData.sendCommandError("generic.level.no_clock");
            return -2;
        }

        if (level.isDarkOutside()) {
            playerData.sendCommandFeedback("cmd.night.error.already_nighttime");
            return -1;
        }

        level.getServer().clockManager().moveToTimeMarker(clock.get(), ClockTimeMarkers.NIGHT);
        playerData.sendCommandFeedback("cmd.night.feedback");
        return 1;
    }
}
