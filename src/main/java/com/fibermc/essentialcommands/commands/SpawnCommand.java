package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.ManagerLocator;
import com.fibermc.essentialcommands.WorldDataManager;
import com.fibermc.essentialcommands.playerdata.PlayerData;
import com.fibermc.essentialcommands.teleportation.PlayerTeleporter;
import com.fibermc.essentialcommands.text.ECText;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;

public class SpawnCommand implements Command<CommandSourceStack> {

    public SpawnCommand() {}

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        WorldDataManager worldDataManager = ManagerLocator.getInstance().getWorldDataManager();
        var loc = worldDataManager.getSpawn();

        var playerData = PlayerData.accessFromContextOrThrow(context);
        if (loc.isEmpty()) {
            playerData.sendCommandError("cmd.spawn.tp.error.no_spawn_set");
            return -2;
        }

        var senderPlayer = context.getSource().getPlayerOrException();

        // Teleport & chat message
        var styledLocationName = ECText.access(senderPlayer).getText("cmd.spawn.location_name");

        PlayerTeleporter.requestTeleport(senderPlayer, loc.get(), styledLocationName);
        return SINGLE_SUCCESS;
    }

}
