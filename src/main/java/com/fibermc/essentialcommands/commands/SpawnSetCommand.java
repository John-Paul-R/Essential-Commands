package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.ManagerLocator;
import com.fibermc.essentialcommands.playerdata.PlayerData;
import com.fibermc.essentialcommands.playerdata.PlayerProfile;
import com.fibermc.essentialcommands.types.MinecraftLocation;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.server.command.ServerCommandSource;

public class SpawnSetCommand implements Command<ServerCommandSource> {

    public SpawnSetCommand() {}

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var worldDataManager = ManagerLocator.getInstance().getWorldDataManager();
        var senderPlayer = context.getSource().getPlayerOrThrow();
        var playerData = PlayerData.access(senderPlayer);

        //Set spawn
        var loc = new MinecraftLocation(senderPlayer);
        worldDataManager.setSpawn(loc);

        //inform command sender that the home has been set
        playerData.sendCommandFeedback(
            "cmd.spawn.set.feedback",
            loc.toText(PlayerProfile.access(senderPlayer))
        );

        return SINGLE_SUCCESS;
    }
}
