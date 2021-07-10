package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.config.Config;
import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.util.TextUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.Texts;

public class NicknameSetCommand implements Command<ServerCommandSource>  {
    public NicknameSetCommand() {}

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        //Store command sender
        ServerPlayerEntity senderPlayerEntity = source.getPlayer();

        //Get specified new nickname
        Text nickname = TextArgumentType.getTextArgument(context, "nickname");
        MutableText nicknameText = null;
        // If nickname is not null and not empty string
        if ( nickname != null && !"".equals(nickname.getString()) ) {
            nicknameText = Texts.parse(context.getSource(), nickname, senderPlayerEntity, 0);
        }

        return exec(context, nicknameText);
    }

    public static int runStringToText(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        NicknameSetCommand.exec(context, TextUtil.parseText(StringArgumentType.getString(context, "nickname_placeholder_api")));
        return 1;
    }

    public static ServerPlayerEntity getTargetPlayer(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity targetPlayer;
        try {
            targetPlayer = EntityArgumentType.getPlayer(context, "target");
        } catch (IllegalArgumentException e) {
            targetPlayer = context.getSource().getPlayer();
        }
        return targetPlayer;
    }

    public static int exec(CommandContext<ServerCommandSource> context, Text nicknameText) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        //Store command sender
        ServerPlayerEntity senderPlayerEntity = context.getSource().getPlayer();

        ServerPlayerEntity targetPlayer = getTargetPlayer(context);

        ServerPlayerEntityAccess targetPlayerEntityAccess = (ServerPlayerEntityAccess) targetPlayer;
        int successCode = targetPlayerEntityAccess.getEcPlayerData().setNickname(nicknameText);

        //inform command sender that the nickname has been set
        if (successCode >= 0) {
            source.sendFeedback(TextUtil.concat(
                new LiteralText("Nickname set to '").setStyle(Config.FORMATTING_DEFAULT),
                (nicknameText != null) ? nicknameText : new LiteralText(senderPlayerEntity.getGameProfile().getName()),
                new LiteralText("'.").setStyle(Config.FORMATTING_DEFAULT)
            ), Config.BROADCAST_TO_OPS);
        } else {
            String failReason = switch (successCode) {
                case -1 -> "Player has insufficient permissions for specified nickname.";
                case -2 -> String.format(
                    "Length of supplied nickname (%s) exceeded max nickname length (%s)",
                    nicknameText.getString().length(),
                    Config.NICKNAME_MAX_LENGTH
                );
                default -> "Unknown";
            };
            source.sendError(TextUtil.concat(
                new LiteralText("Nickname could not be set to '").setStyle(Config.FORMATTING_ERROR),
                nicknameText,
                new LiteralText("'. Reason: ").setStyle(Config.FORMATTING_ERROR),
                new LiteralText(failReason).setStyle(Config.FORMATTING_ERROR)
            ));
        }

        return successCode;
    }

}
