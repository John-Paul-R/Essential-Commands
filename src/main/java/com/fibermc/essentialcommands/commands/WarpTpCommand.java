package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.ManagerLocator;
import com.fibermc.essentialcommands.PlayerTeleporter;
import com.fibermc.essentialcommands.WorldDataManager;
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

public class WarpTpCommand implements Command<ServerCommandSource> {

    public WarpTpCommand() {
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        WorldDataManager worldDataManager = ManagerLocator.INSTANCE.getWorldDataManager();
        int out;
        ServerCommandSource source = context.getSource();
        //Store command sender
        ServerPlayerEntity senderPlayer = source.getPlayer();
        //Store home name
        String warpName = StringArgumentType.getString(context, "warp_name");

        //Get home location
        MinecraftLocation loc = worldDataManager.getWarp(warpName);

        // Teleport & chat message
        if (loc != null) {
            //Teleport player to home location
            PlayerTeleporter.requestTeleport(senderPlayer, loc, "warp:" + warpName);
            out = 1;
        } else {
            Message msg = new LiteralMessage("No warp with the name '" + warpName + "' could be found.");
            throw new CommandSyntaxException(new SimpleCommandExceptionType(msg), msg);
        }

        return out;
    }

}
