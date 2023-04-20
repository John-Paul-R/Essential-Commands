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

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class HomeSetCommand implements Command<ServerCommandSource> {
    public HomeSetCommand() {}

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        String homeName = StringArgumentType.getString(context, "home_name");

        return exec(context, homeName);
    }

    public int runDefault(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return exec(context, "unnamed");
    }

    private static int exec(CommandContext<ServerCommandSource> context, String homeName) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity senderPlayer = source.getPlayerOrThrow();
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
            Text homeNameText = ECText.access(senderPlayer).accent(homeName);
            playerData.addHome(homeName, new MinecraftLocation(senderPlayer));

            playerData.save();
            //inform command sender that the home has been set
            playerData.sendCommandFeedback("cmd.home.set.feedback", homeNameText);
        }

        return 0;
    }
}
