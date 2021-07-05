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

public class WarpSetCommand implements Command<ServerCommandSource> {

    public WarpSetCommand() {}

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        WorldDataManager worldDataManager = ManagerLocator.INSTANCE.getWorldDataManager();

        ServerCommandSource source = context.getSource();
        //Store command sender
        ServerPlayerEntity senderPlayer = source.getPlayer();
        //Store home name
        String warpName = StringArgumentType.getString(context, "warp_name");

        //Add warp
        worldDataManager.setWarp(warpName, new MinecraftLocation(senderPlayer));

        //inform command sender that the home has been set
        source.sendFeedback(
            new LiteralText("Warp '").setStyle(Config.FORMATTING_DEFAULT)
                .append(new LiteralText(warpName).setStyle(Config.FORMATTING_ACCENT))
                .append(new LiteralText("' set.").setStyle(Config.FORMATTING_DEFAULT))
            , Config.BROADCAST_TO_OPS);

        return 1;
    }
}
