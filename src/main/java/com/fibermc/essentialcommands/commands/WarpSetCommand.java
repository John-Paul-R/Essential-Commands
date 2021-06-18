package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.Config;
import com.fibermc.essentialcommands.PlayerData;
import com.fibermc.essentialcommands.PlayerDataManager;
import com.fibermc.essentialcommands.WorldDataManager;
import com.fibermc.essentialcommands.types.MinecraftLocation;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

import java.util.UUID;

public class WarpSetCommand implements Command<ServerCommandSource> {

    private final WorldDataManager worldDataManager;
    public WarpSetCommand(WorldDataManager worldDataManager) {
        this.worldDataManager = worldDataManager;
    }
    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {

        //Store command sender
        ServerPlayerEntity senderPlayer = context.getSource().getPlayer();
        //Store home name
        String warpName = StringArgumentType.getString(context, "warp_name");

        int successCode = 1;

        //Add warp
        worldDataManager.setWarp(warpName, new MinecraftLocation(senderPlayer));

        //inform command sender that the home has been set
        senderPlayer.sendSystemMessage(
                new LiteralText("Warp '").formatted(Config.FORMATTING_DEFAULT)
                        .append(new LiteralText(warpName).formatted(Config.FORMATTING_ACCENT))
                        .append(new LiteralText("' set.").formatted(Config.FORMATTING_DEFAULT))
                , new UUID(0, 0));

        return successCode;
    }
}
