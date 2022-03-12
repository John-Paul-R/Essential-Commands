package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.ECText;
import com.fibermc.essentialcommands.ManagerLocator;
import com.fibermc.essentialcommands.WorldDataManager;
import com.fibermc.essentialcommands.util.TextUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;

public class WarpDeleteCommand implements Command<ServerCommandSource> {

    public WarpDeleteCommand() {}

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        WorldDataManager worldDataManager = ManagerLocator.getInstance().getWorldDataManager();
        ServerCommandSource source = context.getSource();
        String warpName = StringArgumentType.getString(context, "warp_name");

        boolean wasSuccessful = worldDataManager.delWarp(warpName);

        //inform command sender that the warp has been removed
        if (!wasSuccessful) {
            source.sendFeedback(TextUtil.concat(
                ECText.getInstance().getText("cmd.warp.feedback.1").setStyle(CONFIG.FORMATTING_ERROR.getValue()),
                new LiteralText(warpName).setStyle(CONFIG.FORMATTING_ACCENT.getValue()),
                ECText.getInstance().getText("cmd.warp.delete.error.2").setStyle(CONFIG.FORMATTING_ERROR.getValue())
            ), CONFIG.BROADCAST_TO_OPS.getValue());
            return 0;
        }

        source.sendFeedback(TextUtil.concat(
            ECText.getInstance().getText("cmd.warp.feedback.1").setStyle(CONFIG.FORMATTING_DEFAULT.getValue()),
            new LiteralText(warpName).setStyle(CONFIG.FORMATTING_ACCENT.getValue()),
            ECText.getInstance().getText("cmd.warp.delete.feedback.2").setStyle(CONFIG.FORMATTING_DEFAULT.getValue())
        ), CONFIG.BROADCAST_TO_OPS.getValue());
        return 1;
    }
}
