package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.*;
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
import net.minecraft.text.Text;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;

public class WarpTpCommand implements Command<ServerCommandSource> {

    public WarpTpCommand() {}

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        WorldDataManager worldDataManager = ManagerLocator.getInstance().getWorldDataManager();
        ServerPlayerEntity senderPlayer = context.getSource().getPlayer();
        String warpName = StringArgumentType.getString(context, "warp_name");
        var warpNameText = Text.literal(warpName).setStyle(CONFIG.FORMATTING_ACCENT.getValue());
        WarpLocation loc = worldDataManager.getWarp(warpName);

        if (loc == null) {
            Message msg = ECText.getInstance().getText(
                "cmd.warp.tp.error.not_found",
                TextFormatType.Error,
                warpNameText);
            throw new CommandSyntaxException(new SimpleCommandExceptionType(msg), msg);
        }

        if (!loc.hasPermission(senderPlayer)) {
            throw new CommandException(ECText.getInstance().getText(
                "cmd.warp.tp.error.permission",
                TextFormatType.Error,
                warpNameText));
        }

        // Teleport & chat message
        PlayerTeleporter.requestTeleport(
            senderPlayer,
            loc,
            ECText.getInstance().getText("cmd.warp.location_name", warpNameText));

        return 1;
    }

}
