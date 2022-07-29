package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.*;
import com.fibermc.essentialcommands.types.MinecraftLocation;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

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

        ServerPlayerEntity senderPlayer = context.getSource().getPlayer();

        // Teleport & chat message
        var styledLocationName = ECText.getInstance().getText(
            "cmd.spawn.location_name",
            TextFormatType.Default,
            PlayerProfile.accessFromContextOrThrow(context));

        PlayerTeleporter.requestTeleport(senderPlayer, loc, styledLocationName);
        return 1;
    }

}
