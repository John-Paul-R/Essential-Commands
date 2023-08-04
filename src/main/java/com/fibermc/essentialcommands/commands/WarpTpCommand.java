package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.ManagerLocator;
import com.fibermc.essentialcommands.teleportation.PlayerTeleporter;
import com.fibermc.essentialcommands.text.ECText;
import com.fibermc.essentialcommands.text.TextFormatType;
import com.fibermc.essentialcommands.types.WarpLocation;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class WarpTpCommand implements Command<ServerCommandSource> {

    public WarpTpCommand() {}

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity senderPlayer = context.getSource().getPlayer();
        exec(context, senderPlayer);

        return SINGLE_SUCCESS;
    }

    private void exec(
        CommandContext<ServerCommandSource> context,
        ServerPlayerEntity targetPlayer) throws CommandSyntaxException
    {
        var worldDataManager = ManagerLocator.getInstance().getWorldDataManager();
        var senderPlayer = context.getSource().getPlayerOrThrow();
        var ecText = ECText.access(senderPlayer);

        String warpName = StringArgumentType.getString(context, "warp_name");
        var warpNameText = ecText.accent(warpName);
        WarpLocation loc = worldDataManager.getWarp(warpName);

        if (loc == null) {
            throw CommandUtil.createSimpleException(ecText.getText(
                "cmd.warp.tp.error.not_found",
                TextFormatType.Error,
                warpNameText));
        }

        if (!loc.hasPermission(senderPlayer)) {
            throw CommandUtil.createSimpleException(ecText.getText(
                "cmd.warp.tp.error.permission",
                TextFormatType.Error,
                warpNameText));
        }

        // Teleport & chat message
        PlayerTeleporter.requestTeleport(
            targetPlayer,
            loc,
            ecText.getText("cmd.warp.location_name", warpNameText));
    }

    public int runOther(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        exec(context, EntityArgumentType.getPlayer(context, "target_player"));
        return 0;
    }
}
