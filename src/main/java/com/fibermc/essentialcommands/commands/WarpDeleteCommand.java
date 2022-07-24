package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.ECText;
import com.fibermc.essentialcommands.ManagerLocator;
import com.fibermc.essentialcommands.TextFormatType;
import com.fibermc.essentialcommands.WorldDataManager;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.server.command.ServerCommandSource;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;

public class WarpDeleteCommand implements Command<ServerCommandSource> {

    public WarpDeleteCommand() {}

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        WorldDataManager worldDataManager = ManagerLocator.getInstance().getWorldDataManager();
        ServerCommandSource source = context.getSource();
        String warpName = StringArgumentType.getString(context, "warp_name");

        boolean wasSuccessful = worldDataManager.delWarp(warpName);

        var warpNameText = ECText.accent(warpName);
        //inform command sender that the warp has been removed
        if (!wasSuccessful) {
            source.sendFeedback(
                ECText.getInstance().getText("cmd.warp.delete.error", TextFormatType.Error, warpNameText),
                CONFIG.BROADCAST_TO_OPS
            );
            return 0;
        }

        source.sendFeedback(
            ECText.getInstance().getText("cmd.warp.delete.feedback", warpNameText),
            CONFIG.BROADCAST_TO_OPS
        );
        return 1;
    }
}
