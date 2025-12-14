package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.ManagerLocator;
import com.fibermc.essentialcommands.playerdata.PlayerData;
import com.fibermc.essentialcommands.playerdata.PlayerProfile;
import com.fibermc.essentialcommands.types.MinecraftLocation;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;

public class SpawnSetCommand implements Command<CommandSourceStack> {

    public SpawnSetCommand() {}

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var worldDataManager = ManagerLocator.getInstance().getWorldDataManager();
        var senderPlayer = context.getSource().getPlayerOrException();
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
