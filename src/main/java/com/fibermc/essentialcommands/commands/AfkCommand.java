package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.server.command.ServerCommandSource;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;

public class AfkCommand implements Command<ServerCommandSource> {
    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var source = context.getSource();
        var player = source.getPlayerOrThrow();
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
