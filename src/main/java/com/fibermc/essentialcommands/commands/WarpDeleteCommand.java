package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.ManagerLocator;
import com.fibermc.essentialcommands.WorldDataManager;
import com.fibermc.essentialcommands.playerdata.PlayerData;
import com.fibermc.essentialcommands.text.ECText;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.server.command.ServerCommandSource;

public class WarpDeleteCommand implements Command<ServerCommandSource> {

    public WarpDeleteCommand() {}

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        WorldDataManager worldDataManager = ManagerLocator.getInstance().getWorldDataManager();
        var senderPlayerData = PlayerData.accessFromContextOrThrow(context);
        String warpName = StringArgumentType.getString(context, "warp_name");

        boolean wasSuccessful = worldDataManager.delWarp(warpName);

        var warpNameText = ECText.access(senderPlayerData.getPlayer()).accent(warpName);
        //inform command sender that the warp has been removed
        if (!wasSuccessful) {
            senderPlayerData.sendCommandError("cmd.warp.delete.error", warpNameText);
            return 0;
        }

        senderPlayerData.sendCommandFeedback("cmd.warp.delete.feedback", warpNameText);
        return SINGLE_SUCCESS;
    }
}
