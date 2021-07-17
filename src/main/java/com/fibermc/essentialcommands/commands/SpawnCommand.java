package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.ECText;
import com.fibermc.essentialcommands.ManagerLocator;
import com.fibermc.essentialcommands.PlayerTeleporter;
import com.fibermc.essentialcommands.WorldDataManager;
import com.fibermc.essentialcommands.config.Config;
import com.fibermc.essentialcommands.types.MinecraftLocation;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;


public class SpawnCommand implements Command<ServerCommandSource> {

    public SpawnCommand() {
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        WorldDataManager worldDataManager = ManagerLocator.INSTANCE.getWorldDataManager();
        int out;
        //Store command sender
        ServerPlayerEntity senderPlayer = context.getSource().getPlayer();

        //Get home location
        MinecraftLocation loc = worldDataManager.getSpawn();

        // Teleport & chat message
        if (loc != null) {
            //Teleport player to home location
            PlayerTeleporter.requestTeleport(senderPlayer, loc, ECText.getInstance().getText("cmd.spawn.location_name"));
            out = 1;
        } else {
            context.getSource().sendError(ECText.getInstance().getText("cmd.spawn.tp.error.no_spawn_set").setStyle(Config.FORMATTING_ERROR));
            out = -2;
        }

        return out;
    }

}
