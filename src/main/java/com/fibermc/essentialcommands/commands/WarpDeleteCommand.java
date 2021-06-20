package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.Config;
import com.fibermc.essentialcommands.ManagerLocator;
import com.fibermc.essentialcommands.WorldDataManager;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

import java.util.UUID;

public class WarpDeleteCommand implements Command<ServerCommandSource> {

    public WarpDeleteCommand() {}

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        WorldDataManager worldDataManager = ManagerLocator.INSTANCE.getWorldDataManager();
        int out = 0;
        //Store command sender
        ServerPlayerEntity senderPlayer = context.getSource().getPlayer();
        //Store home name
        String warpName = StringArgumentType.getString(context, "warp_name");

        boolean wasSuccessful = worldDataManager.delWarp(warpName);

        //inform command sender that the home has been removed
        if (wasSuccessful) {
            senderPlayer.sendSystemMessage(
                    new LiteralText("Warp ").formatted(Config.FORMATTING_DEFAULT)
                            .append(new LiteralText(warpName).formatted(Config.FORMATTING_ACCENT))
                            .append(new LiteralText(" has been deleted.").formatted(Config.FORMATTING_DEFAULT))
                    , new UUID(0, 0));
            out = 1;
        } else {
            senderPlayer.sendSystemMessage(
                    new LiteralText("Warp ").formatted(Config.FORMATTING_ERROR)
                            .append(new LiteralText(warpName).formatted(Config.FORMATTING_ACCENT))
                            .append(new LiteralText(" could not be deleted. (Correct spelling?)").formatted(Config.FORMATTING_ERROR))
                    , new UUID(0, 0));
        }


        return out;
    }
}
