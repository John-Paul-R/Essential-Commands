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

public class HomeOverwriteCommand implements Command<ServerCommandSource> {
    public HomeOverwriteCommand() {}

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String homeName = StringArgumentType.getString(context, "home_name");
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity senderPlayer = source.getPlayerOrThrow();
        PlayerData playerData = ((ServerPlayerEntityAccess) senderPlayer).ec$getPlayerData();

        var homeNameText = ECText.access(senderPlayer).accent(homeName);
        playerData.removeHome(homeName);
        playerData.addHome(homeName, new MinecraftLocation(senderPlayer));

        playerData.save();
        //inform command sender that the home has been set
        playerData.sendCommandFeedback("cmd.overwritehome.feedback", homeNameText);

        return 0;
    }
}
