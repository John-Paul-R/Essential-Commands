package com.fibermc.essentialcommands.commands.utility;

import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.playerdata.PlayerData;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class SuicideCommand implements Command<ServerCommandSource> {
    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        PlayerData playerData = ((ServerPlayerEntityAccess) player).ec$getPlayerData();

        if (player.isDead()) {
            playerData.sendCommandError("cmd.suicide.error.already_dead");
            return 0;
        }

        player.kill();

        return SINGLE_SUCCESS;
    }
}
