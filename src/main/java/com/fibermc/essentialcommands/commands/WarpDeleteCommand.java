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
        ServerCommandSource source = context.getSource();
        //Store command sender
        ServerPlayerEntity senderPlayer = context.getSource().getPlayer();
        //Store home name
        String warpName = StringArgumentType.getString(context, "warp_name");

        boolean wasSuccessful = worldDataManager.delWarp(warpName);

        //inform command sender that the home has been removed
        if (wasSuccessful) {
            source.sendFeedback(
                new LiteralText("Warp ").setStyle(Config.FORMATTING_DEFAULT)
                    .append(new LiteralText(warpName).setStyle(Config.FORMATTING_ACCENT))
                    .append(new LiteralText(" has been deleted.").setStyle(Config.FORMATTING_DEFAULT))
                , false);
            out = 1;
        } else {
            source.sendFeedback(
                new LiteralText("Warp ").setStyle(Config.FORMATTING_ERROR)
                    .append(new LiteralText(warpName).setStyle(Config.FORMATTING_ACCENT))
                    .append(new LiteralText(" could not be deleted. (Correct spelling?)").setStyle(Config.FORMATTING_ERROR))
                , false);
        }


        return out;
    }
}
