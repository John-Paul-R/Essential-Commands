package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.PlayerData;
import com.fibermc.essentialcommands.text.ECText;
import com.fibermc.essentialcommands.text.TextFormatType;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import dev.jpcode.eccore.util.TextUtil;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;

public class NicknameSetCommand implements Command<ServerCommandSource> {
    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        //Get specified new nickname
        return exec(context, TextArgumentType.getTextArgument(context, "nickname"));
    }

    public static int runStringToText(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        NicknameSetCommand.exec(context, TextUtil.parseText(StringArgumentType.getString(context, "nickname_placeholder_api")));
        return 1;
    }

    public static int exec(CommandContext<ServerCommandSource> context, Text nicknameText) throws CommandSyntaxException {
        ServerPlayerEntity targetPlayer = CommandUtil.getCommandTargetPlayer(context);
        int successCode = PlayerData.access(targetPlayer).setNickname(nicknameText);

        var senderPlayerData = PlayerData.access(context.getSource().getPlayerOrThrow());
        var ecText = ECText.access(senderPlayerData.getPlayer());

        //inform command sender that the nickname has been set
        if (successCode >= 0) {
            senderPlayerData.sendCommandFeedback(
                "cmd.nickname.set.feedback",
                nicknameText != null ? nicknameText : Text.literal(targetPlayer.getGameProfile().getName())
            );
        } else {
            MutableText failReason = switch (successCode) {
                case -1 -> ecText.getText("cmd.nickname.set.error.perms", TextFormatType.Error);
                case -2 -> ecText.getText(
                    "cmd.nickname.set.error.length", TextFormatType.Error,
                    ecText.accent(String.valueOf(nicknameText.getString().length())),
                    ecText.accent(String.valueOf(CONFIG.NICKNAME_MAX_LENGTH))
                );
                default -> ecText.getText("generic.error.unknown", TextFormatType.Error);
            };
            senderPlayerData.sendCommandError("cmd.nickname.set.error", nicknameText, failReason);
        }

        return successCode;
    }
}
