package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.config.Config;
import com.fibermc.essentialcommands.ManagerLocator;
import com.fibermc.essentialcommands.WorldDataManager;
import com.fibermc.essentialcommands.util.TextUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

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

        //inform command sender that the warp has been removed
        if (wasSuccessful) {
            source.sendFeedback(TextUtil.concat(
                new LiteralText("Warp ").setStyle(Config.FORMATTING_DEFAULT),
                new LiteralText(warpName).setStyle(Config.FORMATTING_ACCENT),
                new LiteralText(" has been deleted.").setStyle(Config.FORMATTING_DEFAULT)
            ), Config.BROADCAST_TO_OPS);
            out = 1;
        } else {
            source.sendFeedback(TextUtil.concat(
                new LiteralText("Warp ").setStyle(Config.FORMATTING_ERROR),
                new LiteralText(warpName).setStyle(Config.FORMATTING_ACCENT),
                new LiteralText(" could not be deleted. (Correct spelling?)").setStyle(Config.FORMATTING_ERROR)
            ), Config.BROADCAST_TO_OPS);
        }


        return out;
    }
}
