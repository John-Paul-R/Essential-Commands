package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.playerdata.PlayerData;
import com.fibermc.essentialcommands.text.ECText;
import com.fibermc.essentialcommands.types.MinecraftLocation;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

public class HomeOverwriteCommand implements Command<CommandSourceStack> {
    public HomeOverwriteCommand() {}

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String homeName = StringArgumentType.getString(context, "home_name");
        CommandSourceStack source = context.getSource();
        ServerPlayer senderPlayer = source.getPlayerOrException();
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
