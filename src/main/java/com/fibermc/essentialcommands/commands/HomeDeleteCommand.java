package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.Config;
import com.fibermc.essentialcommands.PlayerData;
import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

import java.util.UUID;

public class HomeDeleteCommand implements Command<ServerCommandSource> {

    public HomeDeleteCommand() {}

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        int out = 1;
        ServerCommandSource source = context.getSource();
        //Store command sender
        ServerPlayerEntity senderPlayer = source.getPlayer();
        PlayerData senderPlayerData = ((ServerPlayerEntityAccess)senderPlayer).getEcPlayerData();
        //Store home name
        String homeName = StringArgumentType.getString(context, "home_name");

        //Remove Home - TODO Require player to type the command again to confirm deletion.
        boolean wasSuccessful = senderPlayerData.removeHome(homeName);

        //inform command sender that the home has been removed
        if (wasSuccessful) {
            source.sendFeedback(
                new LiteralText("Home ").setStyle(Config.FORMATTING_DEFAULT)
                    .append(new LiteralText(homeName).setStyle(Config.FORMATTING_ACCENT))
                    .append(new LiteralText(" has been deleted.").setStyle(Config.FORMATTING_DEFAULT))
                , Config.BROADCAST_TO_OPS);
        } else {
            source.sendError(
                new LiteralText("Home ").setStyle(Config.FORMATTING_ERROR)
                    .append(new LiteralText(homeName).setStyle(Config.FORMATTING_ACCENT))
                    .append(new LiteralText(" could not be deleted.").setStyle(Config.FORMATTING_ERROR))
            );
            out = 0;
        }

        return out;
    }
}
