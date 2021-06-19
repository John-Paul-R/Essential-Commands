package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.*;
import com.fibermc.essentialcommands.types.MinecraftLocation;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

import java.util.UUID;

public class SpawnCommand implements Command<ServerCommandSource> {

    public SpawnCommand() {}

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
            senderPlayer.sendSystemMessage(
                new LiteralText("Teleporting to ").formatted(Config.FORMATTING_DEFAULT)
                    .append(new LiteralText("spawn").formatted(Config.FORMATTING_ACCENT))
                    .append(new LiteralText("...").formatted(Config.FORMATTING_DEFAULT))
                , new UUID(0, 0));
            //Teleport player to home location
            PlayerTeleporter.teleport(senderPlayer, loc);
            out = 1;
        } else {
            Message msg = new LiteralMessage("Spawn not set.");
            throw new CommandSyntaxException(new SimpleCommandExceptionType(msg), msg);
        }

        return out;
    }

}
