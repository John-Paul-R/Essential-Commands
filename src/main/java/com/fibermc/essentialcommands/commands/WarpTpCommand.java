package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.*;
import com.fibermc.essentialcommands.types.MinecraftLocation;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

import java.util.UUID;

public class WarpTpCommand implements Command<ServerCommandSource> {

    private final PlayerDataManager playerDataManager;
    private final WorldDataManager worldDataManager;
    public WarpTpCommand(PlayerDataManager playerDataManager, WorldDataManager worldDataManager) {
        this.playerDataManager = playerDataManager;
        this.worldDataManager = worldDataManager;
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        int out;
        //Store command sender
        ServerPlayerEntity senderPlayer = context.getSource().getPlayer();
        PlayerData senderPlayerData = playerDataManager.getOrCreate(senderPlayer);
        //Store home name
        String warpName = StringArgumentType.getString(context, "warp_name");

        //Get home location
        MinecraftLocation loc = worldDataManager.getWarp(warpName);

        // Teleport & chat message
        if (loc != null) {
            senderPlayer.sendSystemMessage(
                    new LiteralText("Teleporting to ").formatted(Config.FORMATTING_DEFAULT)
                            .append(new LiteralText(warpName).formatted(Config.FORMATTING_ACCENT))
                            .append(new LiteralText("...").formatted(Config.FORMATTING_DEFAULT))
                    , UUID.randomUUID());
            //Teleport player to home location
            PlayerTeleporter.teleport(senderPlayerData, loc);
            out = 1;
        } else {
            Message msg = new LiteralMessage("No warp with the name '" + warpName + "' could be found.");
            throw new CommandSyntaxException(new SimpleCommandExceptionType(msg), msg);
        }

        return out;
    }

}
