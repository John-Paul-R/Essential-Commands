package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.playerdata.PlayerData;
import com.fibermc.essentialcommands.teleportation.PlayerTeleporter;
import com.fibermc.essentialcommands.text.ECText;
import com.fibermc.essentialcommands.types.MinecraftLocation;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class BackCommand implements Command<ServerCommandSource> {

    public BackCommand() {}

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        //Store command sender
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        PlayerData playerData = ((ServerPlayerEntityAccess) player).ec$getPlayerData();

        //Get previous location
        MinecraftLocation loc = playerData.getPreviousLocation();

        //chat message
        if (loc == null) {
            playerData.sendCommandError("cmd.back.error.no_prev_location");
            return 0;
        }

        //Teleport player to home location
        var prevLocationName = ECText.access(player).getText("cmd.back.location_name");
        PlayerTeleporter.requestTeleport(playerData, loc, prevLocationName);

        return SINGLE_SUCCESS;
    }
}
