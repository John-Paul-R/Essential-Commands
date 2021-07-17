package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.ECText;
import com.fibermc.essentialcommands.ManagerLocator;
import com.fibermc.essentialcommands.WorldDataManager;
import com.fibermc.essentialcommands.config.Config;
import com.fibermc.essentialcommands.types.MinecraftLocation;
import com.fibermc.essentialcommands.util.TextUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;


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
        try {
            worldDataManager.setWarp(warpName, new MinecraftLocation(senderPlayer));
            //inform command sender that the home has been set
            source.sendFeedback(TextUtil.concat(
                    ECText.getInstance().getText("cmd.warp.feedback.1").setStyle(Config.FORMATTING_DEFAULT),
                    new LiteralText(warpName).setStyle(Config.FORMATTING_ACCENT),
                    ECText.getInstance().getText("cmd.warp.set.feedback.2").setStyle(Config.FORMATTING_DEFAULT)
            ), Config.BROADCAST_TO_OPS);
        } catch (CommandSyntaxException e) {
            source.sendError(TextUtil.concat(
                    ECText.getInstance().getText("cmd.warp.feedback.1").setStyle(Config.FORMATTING_ERROR),
                    new LiteralText(warpName).setStyle(Config.FORMATTING_ACCENT),
                    ECText.getInstance().getText("cmd.warp.set.error.exists.2").setStyle(Config.FORMATTING_ERROR)
            ));
        }

        return 1;
    }
}
