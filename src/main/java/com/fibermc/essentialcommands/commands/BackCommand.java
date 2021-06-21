package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.*;
import com.fibermc.essentialcommands.types.MinecraftLocation;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

import java.util.UUID;

public class BackCommand implements Command<ServerCommandSource> {

    public BackCommand() {}

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        PlayerDataManager dataManager = ManagerLocator.INSTANCE.getPlayerDataManager();
        int out = 0;
        //Store command sender
        ServerPlayerEntity player = context.getSource().getPlayer();
        PlayerData playerData = dataManager.getOrCreate(player);

        //Get previous location
        MinecraftLocation loc = playerData.getPreviousLocation();

        //chat message
        if (loc != null) {
            //Teleport player to home location
            PlayerTeleporter.requestTeleport(playerData, loc, "previous location");

            out=1;
        } else {
            player.sendSystemMessage(
                    new LiteralText("Could not execute 'back' command. No previous location found.").setStyle(Config.FORMATTING_ERROR)
                    , new UUID(0, 0));
        }

        return out;
    }
}
