package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.*;
import com.fibermc.essentialcommands.types.MinecraftLocation;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

import java.util.UUID;

public class SpawnSetCommand implements Command<ServerCommandSource> {

    public SpawnSetCommand() {}

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        WorldDataManager worldDataManager = ManagerLocator.INSTANCE.getWorldDataManager();

        //Store command sender
        ServerPlayerEntity senderPlayer = context.getSource().getPlayer();

        int successCode = 1;

        //Set spawn
        MinecraftLocation loc = new MinecraftLocation(senderPlayer);
        worldDataManager.setSpawn(loc);

        //inform command sender that the home has been set
        senderPlayer.sendSystemMessage(
            new LiteralText("Spawn set at ").setStyle(Config.FORMATTING_DEFAULT)
                .append(loc.toLiteralTextSimple().setStyle(Config.FORMATTING_ACCENT))
            , new UUID(0, 0));

        return successCode;
    }
}
