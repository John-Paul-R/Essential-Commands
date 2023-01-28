package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.playerdata.PlayerData;
import com.fibermc.essentialcommands.text.ECText;
import com.fibermc.essentialcommands.types.MinecraftLocation;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class HomeSetCommand implements Command<ServerCommandSource> {

    public HomeSetCommand() {}

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity senderPlayer = source.getPlayer();
        PlayerData playerData = ((ServerPlayerEntityAccess) senderPlayer).ec$getPlayerData();
        String homeName = StringArgumentType.getString(context, "home_name");

        //TODO if home with given name is already set, warn of overwrite and require that the command be typed again, or a confirmation message be given
        var homeNameText = ECText.access(senderPlayer).accent(homeName);
        playerData.addHome(homeName, new MinecraftLocation(senderPlayer));

        playerData.save();
        //inform command sender that the home has been set
        playerData.sendCommandFeedback("cmd.home.set.feedback", homeNameText);

        return 0;
    }
}
