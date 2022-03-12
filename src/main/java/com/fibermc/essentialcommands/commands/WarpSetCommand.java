package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.ECText;
import com.fibermc.essentialcommands.ManagerLocator;
import com.fibermc.essentialcommands.WorldDataManager;
import com.fibermc.essentialcommands.types.MinecraftLocation;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.jpcode.eccore.util.TextUtil;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;


public class WarpSetCommand implements Command<ServerCommandSource> {

    public WarpSetCommand() {}

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        WorldDataManager worldDataManager = ManagerLocator.getInstance().getWorldDataManager();
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity senderPlayer = source.getPlayer();
        String warpName = StringArgumentType.getString(context, "warp_name");

        boolean requiresPermission;
        try {
            requiresPermission = BoolArgumentType.getBool(context, "requires_permission");
        } catch (IllegalArgumentException ign) {
            requiresPermission = false;
        }

        //Add warp
        try {
            worldDataManager.setWarp(warpName, new MinecraftLocation(senderPlayer), requiresPermission);
            //inform command sender that the home has been set
            source.sendFeedback(TextUtil.concat(
                    ECText.getInstance().getText("cmd.warp.feedback.1").setStyle(CONFIG.FORMATTING_DEFAULT.getValue()),
                    new LiteralText(warpName).setStyle(CONFIG.FORMATTING_ACCENT.getValue()),
                    ECText.getInstance().getText("cmd.warp.set.feedback.2").setStyle(CONFIG.FORMATTING_DEFAULT.getValue())
            ), CONFIG.BROADCAST_TO_OPS.getValue());
        } catch (CommandSyntaxException e) {
            source.sendError(TextUtil.concat(
                    ECText.getInstance().getText("cmd.warp.feedback.1").setStyle(CONFIG.FORMATTING_ERROR.getValue()),
                    new LiteralText(warpName).setStyle(CONFIG.FORMATTING_ACCENT.getValue()),
                    ECText.getInstance().getText("cmd.warp.set.error.exists.2").setStyle(CONFIG.FORMATTING_ERROR.getValue())
            ));
        }

        return 1;
    }
}
