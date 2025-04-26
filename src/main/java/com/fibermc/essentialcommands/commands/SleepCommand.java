package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.playerdata.PlayerData;
import com.fibermc.essentialcommands.util.PlayerUtilities;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.server.command.ServerCommandSource;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;

public class SleepCommand implements Command<ServerCommandSource> {
    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var source = context.getSource();
        var player = source.getPlayerOrThrow();
        var playerAccess = (ServerPlayerEntityAccess)player;
        var playerData = PlayerData.access(player);
        var pos = player.getBlockPos();

        if (player.isSleeping()) {
            player.wakeUp();
            return SINGLE_SUCCESS;
        }

        if (!CONFIG.SLEEP_NEAR_MONSTERS && PlayerUtilities.isNearAngryMonsters(player)) {
            // todo: use mc text?
            PlayerData.access(player).sendError("cmd.sleep.error.near_monsters");
            return 0;
        }

        player.sleep(pos);
        playerData.setIsSleepingFromCommand(true);

        return SINGLE_SUCCESS;
    }
}
