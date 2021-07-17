package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.ECText;
import com.fibermc.essentialcommands.PlayerData;
import com.fibermc.essentialcommands.PlayerTeleporter;
import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.config.Config;
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
        int out = 0;
        //Store command sender
        ServerPlayerEntity player = context.getSource().getPlayer();
        PlayerData playerData = ((ServerPlayerEntityAccess)player).getEcPlayerData();

        //Get previous location
        MinecraftLocation loc = playerData.getPreviousLocation();

        //chat message
        if (loc != null) {
            //Teleport player to home location
            PlayerTeleporter.requestTeleport(playerData, loc, ECText.getInstance().getText("cmd.back.location_name"));

            out=1;
        } else {
            context.getSource().sendError(
                ECText.getInstance().getText("cmd.back.error.no_prev_location").setStyle(Config.FORMATTING_ERROR)
            );
        }

        return out;
    }
}
