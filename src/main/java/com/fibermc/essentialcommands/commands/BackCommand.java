package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.Config;
import com.fibermc.essentialcommands.PlayerData;
import com.fibermc.essentialcommands.PlayerDataManager;
import com.fibermc.essentialcommands.PlayerTeleporter;
import com.fibermc.essentialcommands.types.MinecraftLocation;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

import java.util.UUID;

public class BackCommand implements Command<ServerCommandSource> {

    private PlayerDataManager dataManager;
    public BackCommand(PlayerDataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        int out = 0;
        //Store command sender
        ServerPlayerEntity player = context.getSource().getPlayer();
        PlayerData playerData = dataManager.getOrCreate(player);

        //Get previous location
        MinecraftLocation loc = playerData.getPreviousLocation();

        //chat message
        if (loc != null) {
            player.sendSystemMessage(
                    new LiteralText("Teleporting to previous location...").formatted(Config.FORMATTING_DEFAULT),
                    new UUID(0, 0));
            //Teleport player to home location
            PlayerTeleporter.teleport(playerData, loc);

            out=1;
        } else {
            player.sendSystemMessage(
                    new LiteralText("Could not execute 'back' command. No previous location found.").formatted(Config.FORMATTING_ERROR)
                    , new UUID(0, 0));
        }

        return out;
    }
}
