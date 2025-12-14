package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.playerdata.PlayerData;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public class NicknameClearCommand implements Command<CommandSourceStack> {
    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var senderPlayerData = PlayerData.accessFromContextOrThrow(context);

        var targetPlayer = CommandUtil.getCommandTargetPlayer(context);
        var targetPlayerData = PlayerData.access(targetPlayer);

        targetPlayerData.setNickname(null);

        //inform command sender that the nickname has been set
        senderPlayerData.sendCommandFeedback(
            "cmd.nickname.set.feedback",
            Component.literal(targetPlayer.getGameProfile().name())
        );

        return SINGLE_SUCCESS;
    }
}
