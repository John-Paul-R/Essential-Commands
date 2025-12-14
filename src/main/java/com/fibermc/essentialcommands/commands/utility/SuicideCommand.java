package com.fibermc.essentialcommands.commands.utility;

import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.playerdata.PlayerData;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

public class SuicideCommand implements Command<CommandSourceStack> {
    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayer();
        PlayerData playerData = ((ServerPlayerEntityAccess) player).ec$getPlayerData();

        if (player.isDeadOrDying()) {
            playerData.sendCommandError("cmd.suicide.error.already_dead");
            return 0;
        }

        player.kill(player.level());

        return SINGLE_SUCCESS;
    }
}
