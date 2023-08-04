package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.playerdata.PlayerData;
import com.fibermc.essentialcommands.text.ECText;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class HomeDeleteCommand implements Command<ServerCommandSource> {

    public HomeDeleteCommand() {}

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        //Store command sender
        ServerPlayerEntity senderPlayer = source.getPlayerOrThrow();
        PlayerData senderPlayerData = ((ServerPlayerEntityAccess) senderPlayer).ec$getPlayerData();
        //Store home name
        String homeName = StringArgumentType.getString(context, "home_name");

        //Remove Home - TODO Require player to type the command again to confirm deletion.
        boolean wasSuccessful = senderPlayerData.removeHome(homeName);

        var homeNameText = ECText.access(senderPlayer).accent(homeName);
        //inform command sender that the home has been removed
        if (wasSuccessful) {
            senderPlayerData.sendCommandFeedback("cmd.home.delete.feedback", homeNameText);
            return SINGLE_SUCCESS;
        }

        senderPlayerData.sendCommandError("cmd.home.delete.error", homeNameText);
        return 0;
    }
}
