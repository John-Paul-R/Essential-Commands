package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.playerdata.PlayerData;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.server.command.ServerCommandSource;

import dev.jpcode.eccore.util.TextUtil;

public class NicknameClearCommand implements Command<ServerCommandSource> {
    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var senderPlayerData = PlayerData.accessFromContextOrThrow(context);

        var targetPlayer = CommandUtil.getCommandTargetPlayer(context);
        var targetPlayerData = PlayerData.access(targetPlayer);

        targetPlayerData.setNickname(null);

        //inform command sender that the nickname has been set
        senderPlayerData.sendCommandFeedback(
            "cmd.nickname.set.feedback",
            TextUtil.literal(targetPlayer.getGameProfile().getName())
        );

        return 1;
    }
}
