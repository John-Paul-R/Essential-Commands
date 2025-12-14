package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.playerdata.PlayerData;
import com.fibermc.essentialcommands.util.PlayerUtilities;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;

public class SleepCommand implements Command<CommandSourceStack> {
    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var source = context.getSource();
        var player = source.getPlayerOrException();
        var playerData = PlayerData.access(player);
        var pos = player.blockPosition();

        if (player.isSleeping()) {
            player.stopSleeping();
            return SINGLE_SUCCESS;
        }

        if (!CONFIG.SLEEP_NEAR_MONSTERS && PlayerUtilities.isNearAngryMonsters(player)) {
            PlayerData.access(player).sendCommandError(Component.translatable("block.minecraft.bed.not_safe"));
            return 0;
        }

        player.startSleeping(pos);
        playerData.setIsSleepingFromCommand(true);

        return SINGLE_SUCCESS;
    }
}
