package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.playerdata.PlayerData;
import com.fibermc.essentialcommands.text.ChatConfirmationPrompt;
import com.fibermc.essentialcommands.text.ECText;
import com.fibermc.essentialcommands.types.MinecraftLocation;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class HomeSetCommand implements Command<CommandSourceStack> {
    public HomeSetCommand() {}

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String homeName = StringArgumentType.getString(context, "home_name");

        return exec(context, homeName);
    }

    public int runDefault(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return exec(context, "unnamed");
    }

    private static int exec(CommandContext<CommandSourceStack> context, String homeName) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer senderPlayer = source.getPlayerOrException();
        PlayerData playerData = ((ServerPlayerEntityAccess) senderPlayer).ec$getPlayerData();

        if (playerData.existsHome(homeName)) {
            //ask the player whether they want to override the home
            ECText playerEcText = ECText.access(senderPlayer);
            playerData.sendMessage(
                "cmd.home.set.overwrite",
                playerEcText.accent(homeName)
            );

            new ChatConfirmationPrompt(
                senderPlayer,
                "/essentialcommands overwritehome " + homeName,
                playerEcText.accent("[" + ECText.getInstance().getString("generic.confirm") + "]")
            ).send();
        } else {
            Component homeNameText = ECText.access(senderPlayer).accent(homeName);
            playerData.addHome(homeName, new MinecraftLocation(senderPlayer));

            playerData.save();
            //inform command sender that the home has been set
            playerData.sendCommandFeedback("cmd.home.set.feedback", homeNameText);
        }

        return 0;
    }
}
