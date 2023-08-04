package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.ManagerLocator;
import com.fibermc.essentialcommands.WorldDataManager;
import com.fibermc.essentialcommands.playerdata.PlayerData;
import com.fibermc.essentialcommands.text.ECText;
import com.fibermc.essentialcommands.types.MinecraftLocation;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.server.command.ServerCommandSource;

public class WarpSetCommand implements Command<ServerCommandSource> {

    public WarpSetCommand() {}

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        WorldDataManager worldDataManager = ManagerLocator.getInstance().getWorldDataManager();
        var senderPlayer = context.getSource().getPlayerOrThrow();
        var senderPlayerData = PlayerData.access(senderPlayer);
        String warpName = StringArgumentType.getString(context, "warp_name");

        boolean requiresPermission;
        try {
            requiresPermission = BoolArgumentType.getBool(context, "requires_permission");
        } catch (IllegalArgumentException ign) {
            requiresPermission = false;
        }

        var warpNameText = ECText.access(senderPlayer).accent(warpName);
        //Add warp
        try {
            worldDataManager.setWarp(warpName, new MinecraftLocation(senderPlayer), requiresPermission);
            //inform command sender that the home has been set
            senderPlayerData.sendCommandFeedback("cmd.warp.set.feedback", warpNameText);
        } catch (CommandSyntaxException e) {
            senderPlayerData.sendCommandError("cmd.warp.set.error.exists", warpNameText);
        }

        return SINGLE_SUCCESS;
    }
}
