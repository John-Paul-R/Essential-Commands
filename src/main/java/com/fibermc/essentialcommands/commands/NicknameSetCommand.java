package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.ECPerms;
import com.fibermc.essentialcommands.commands.helpers.FeedbackReceiver;
import com.fibermc.essentialcommands.playerdata.PlayerData;
import com.fibermc.essentialcommands.text.ECText;
import com.fibermc.essentialcommands.text.TextFormatType;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;

import dev.jpcode.eccore.util.TextUtil;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;

public class NicknameSetCommand implements Command<ServerCommandSource> {
    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return exec(context, TextArgumentType.getTextArgument(context, "nickname"));
    }

    public static int runStringToText(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        NicknameSetCommand.exec(context, TextUtil.parseText(StringArgumentType.getString(context, "nickname_placeholder_api")));
        return SINGLE_SUCCESS;
    }

    public static int exec(CommandContext<ServerCommandSource> context, Text rawNicknameText) throws CommandSyntaxException {
        ServerPlayerEntity targetPlayer = CommandUtil.getCommandTargetPlayer(context);

        var nicknameWithContext = ECPerms.check(context.getSource(), ECPerms.Registry.nickname_selector_and_ctx, 2)
            ? Texts.parse(
                context.getSource(),
                rawNicknameText,
                targetPlayer,
                0)
            : rawNicknameText;

        var nicknameText = ECPerms.check(context.getSource(), ECPerms.Registry.nickname_placeholders, 2)
            ? Placeholders.parseText(nicknameWithContext, PlaceholderContext.of(targetPlayer))
            : nicknameWithContext;
        int successCode = PlayerData.access(targetPlayer).setNickname(nicknameText);

        var senderPlayer = context.getSource().getPlayer();
        var senderFeedbackReceiver = FeedbackReceiver.ofSource(context.getSource());

        var ecText = ECText.access(senderPlayer);

        //inform command sender that the nickname has been set
        if (successCode >= 0) {
            senderFeedbackReceiver.sendCommandFeedback(
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
            senderFeedbackReceiver.sendCommandError("cmd.nickname.set.error", nicknameText, failReason);
        }

        return successCode;
    }
}
