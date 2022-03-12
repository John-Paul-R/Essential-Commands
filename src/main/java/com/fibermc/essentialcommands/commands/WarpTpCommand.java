package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.ECText;
import com.fibermc.essentialcommands.ManagerLocator;
import com.fibermc.essentialcommands.PlayerTeleporter;
import com.fibermc.essentialcommands.WorldDataManager;
import com.fibermc.essentialcommands.types.WarpLocation;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.command.CommandException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class WarpTpCommand implements Command<ServerCommandSource> {

    public WarpTpCommand() {}

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        WorldDataManager worldDataManager = ManagerLocator.getInstance().getWorldDataManager();
        ServerPlayerEntity senderPlayer = context.getSource().getPlayer();
        String warpName = StringArgumentType.getString(context, "warp_name");

        WarpLocation loc = worldDataManager.getWarp(warpName);

        if (loc == null) {
            Message msg = ECText.getInstance().getText("cmd.warp.tp.error.not_found", warpName);
            throw new CommandSyntaxException(new SimpleCommandExceptionType(msg), msg);
        }

        if (!loc.hasPermission(senderPlayer)) {
            throw new CommandException(ECText.getInstance().getText("cmd.warp.tp.error.permission", warpName));
        }

        // Teleport & chat message
        PlayerTeleporter.requestTeleport(
            senderPlayer,
            loc,
            ECText.getInstance().getText("cmd.warp.location_name", warpName));

        return 1;
    }

}
