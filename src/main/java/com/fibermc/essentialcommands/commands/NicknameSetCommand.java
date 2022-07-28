package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.ECText;
import com.fibermc.essentialcommands.TextFormatType;
import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;

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
    public NicknameSetCommand() {}

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
        ServerCommandSource source = context.getSource();

        ServerPlayerEntity targetPlayer = CommandUtil.getCommandTargetPlayer(context);

        ServerPlayerEntityAccess targetPlayerEntityAccess = (ServerPlayerEntityAccess) targetPlayer;
        int successCode = targetPlayerEntityAccess.ec$getPlayerData().setNickname(nicknameText);

        //inform command sender that the nickname has been set
        if (successCode >= 0) {
            source.sendFeedback(
                ECText.getInstance().getText(
                    "cmd.nickname.set.feedback",
                    (nicknameText != null) ? nicknameText : Text.literal(targetPlayer.getGameProfile().getName())),
                CONFIG.BROADCAST_TO_OPS);
        } else {
            MutableText failReason = switch (successCode) {
                case -1 -> ECText.getInstance().getText("cmd.nickname.set.error.perms");
                case -2 -> ECText.getInstance().getText(
                    "cmd.nickname.set.error.length",
                    nicknameText.getString().length(),
                    CONFIG.NICKNAME_MAX_LENGTH
                );
                default -> ECText.getInstance().getText("generic.error.unknown");
            };
            source.sendError(
                ECText.getInstance().getText(
                    "cmd.nickname.set.error",
                    TextFormatType.Error,
                    nicknameText,
                    failReason.setStyle(CONFIG.FORMATTING_ERROR))
            );
        }

        return successCode;
    }

}
