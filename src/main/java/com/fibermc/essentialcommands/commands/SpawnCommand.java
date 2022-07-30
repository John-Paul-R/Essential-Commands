package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.*;
import com.fibermc.essentialcommands.types.MinecraftLocation;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.server.command.ServerCommandSource;

public class SpawnCommand implements Command<ServerCommandSource> {

    public SpawnCommand() {}

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        WorldDataManager worldDataManager = ManagerLocator.getInstance().getWorldDataManager();
        MinecraftLocation loc = worldDataManager.getSpawn();

        var playerData = PlayerData.accessFromContextOrThrow(context);
        if (loc == null) {
            playerData.sendCommandError("cmd.spawn.tp.error.no_spawn_set");
            return -2;
        }

        var senderPlayer = context.getSource().getPlayerOrThrow();

        // Teleport & chat message
        var styledLocationName = ECText.access(senderPlayer).getText("cmd.spawn.location_name");

        PlayerTeleporter.requestTeleport(senderPlayer, loc, styledLocationName);
        return 1;
    }

}
