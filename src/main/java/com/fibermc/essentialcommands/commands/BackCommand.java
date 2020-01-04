package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.Config;
import com.fibermc.essentialcommands.PlayerData;
import com.fibermc.essentialcommands.PlayerDataManager;
import com.fibermc.essentialcommands.PlayerTeleporter;
import com.fibermc.essentialcommands.types.MinecraftLocation;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.network.MessageType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

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
            player.sendChatMessage(
                    new LiteralText("Teleporting to previous location...").formatted(Config.FORMATTING_DEFAULT)
                    , MessageType.SYSTEM);
            out=1;
        } else {
            player.sendChatMessage(
                    new LiteralText("Could not execute 'back' command. No previous location found.").formatted(Config.FORMATTING_ERROR)
                    , MessageType.SYSTEM);
        }

        //Teleport player to home location
        PlayerTeleporter.teleport(playerData, loc);

        return out;
    }
}
