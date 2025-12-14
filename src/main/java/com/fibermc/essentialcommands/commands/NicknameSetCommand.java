package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.ECPerms;
import com.fibermc.essentialcommands.commands.helpers.FeedbackReceiver;
import com.fibermc.essentialcommands.playerdata.PlayerData;
import com.fibermc.essentialcommands.text.ECText;
import com.fibermc.essentialcommands.text.TextFormatType;
import eu.pb4.placeholders.api.ParserContext;
import eu.pb4.placeholders.api.node.TextNode;
import eu.pb4.placeholders.api.parsers.TagParser;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

import dev.jpcode.eccore.util.TextUtil;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;

public class NicknameSetCommand implements Command<CommandSourceStack> {
    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return exec(context, ComponentArgument.getRawComponent(context, "nickname"));
    }

    public static int runStringToText(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        NicknameSetCommand.exec(context, TextUtil.parseText(StringArgumentType.getString(context, "nickname_placeholder_api")));
        return SINGLE_SUCCESS;
    }

    public static int exec(CommandContext<CommandSourceStack> context, Component rawNicknameText) throws CommandSyntaxException {
        ServerPlayer targetPlayer = CommandUtil.getCommandTargetPlayer(context);

        var nicknameWithContext = ECPerms.check(context.getSource(), ECPerms.Registry.nickname_selector_and_ctx, 2)
            ? ComponentUtils.updateForEntity(
                context.getSource(),
                rawNicknameText,
                targetPlayer,
                0)
            : rawNicknameText;

        var nicknameText = ECPerms.check(context.getSource(), ECPerms.Registry.nickname_placeholders, 2)
            ? TagParser.DEFAULT_SAFE.parseText(TextNode.convert(nicknameWithContext), ParserContext.of())
            : nicknameWithContext;
        int successCode = PlayerData.access(targetPlayer).setNickname(nicknameText);

        var senderPlayer = context.getSource().getPlayer();
        var senderFeedbackReceiver = FeedbackReceiver.ofSource(context.getSource());

        var ecText = ECText.access(senderPlayer);

        //inform command sender that the nickname has been set
        if (successCode >= 0) {
            senderFeedbackReceiver.sendCommandFeedback(
                "cmd.nickname.set.feedback",
                nicknameText != null ? nicknameText : Component.literal(targetPlayer.getGameProfile().name())
            );
        } else {
            MutableComponent failReason = switch (successCode) {
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
