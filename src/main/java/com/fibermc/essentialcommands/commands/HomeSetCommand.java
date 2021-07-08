package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.Config;
import com.fibermc.essentialcommands.PlayerData;
import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.types.MinecraftLocation;
import com.fibermc.essentialcommands.util.TextUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

import java.util.UUID;

public class HomeSetCommand implements Command<ServerCommandSource> {

    public HomeSetCommand() {}

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        //Store command sender
        ServerPlayerEntity senderPlayer = source.getPlayer();
        //Store home name
        String homeName = StringArgumentType.getString(context, "home_name");

        //Add home to PlayerData
        //TODO if home with given name is already set, warn of overwrite and require that the command be typed again, or a confirmation message be given
        PlayerData pData = ((ServerPlayerEntityAccess)senderPlayer).getEcPlayerData();
        int successCode = pData.addHome(homeName, new MinecraftLocation(senderPlayer));
        pData.markDirty();
        pData.save();
        //inform command sender that the home has been set
        if (successCode == 1) {
            source.sendFeedback(
                new LiteralText("Home '").setStyle(Config.FORMATTING_DEFAULT)
                    .append(new LiteralText(homeName).setStyle(Config.FORMATTING_ACCENT))
                    .append(new LiteralText("' set.").setStyle(Config.FORMATTING_DEFAULT)),
                Config.BROADCAST_TO_OPS
            );
        } else if (successCode==0) {
            source.sendError(TextUtil.concat(
                new LiteralText("Home '").setStyle(Config.FORMATTING_ERROR),
                new LiteralText(homeName).setStyle(Config.FORMATTING_ACCENT),
                new LiteralText("' could not be set. Home limit (").setStyle(Config.FORMATTING_ERROR),
                new LiteralText(String.valueOf(Config.HOME_LIMIT)).setStyle(Config.FORMATTING_ACCENT),
                new LiteralText(") reached.").setStyle(Config.FORMATTING_ERROR)
            ));
        }


        return successCode;
    }
}
