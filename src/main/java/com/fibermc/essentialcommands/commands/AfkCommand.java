package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;

public class AfkCommand implements Command<CommandSourceStack> {
    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var source = context.getSource();
        var player = source.getPlayerOrException();
        var playerAccess = ((ServerPlayerEntityAccess) player);
        var playerData = playerAccess.ec$getPlayerData();

        if (CONFIG.INVULN_WHILE_AFK) {
            if (playerData.isInCombat()) {
                playerData.sendError("cmd.afk.error.in_combat");
                return 0;
            }
        }

        // Message sending is done in here
        playerData.setAfk(!playerData.isAfk());
        playerData.updateLastActionTick();

        return 0;
    }
}
