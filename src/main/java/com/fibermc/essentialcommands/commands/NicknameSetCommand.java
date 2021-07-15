package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.ECText;
import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.config.Config;
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
        //Get specified new nickname
        return exec(context, TextArgumentType.getTextArgument(context, "nickname"));
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

        ServerPlayerEntity targetPlayer = getTargetPlayer(context);

        ServerPlayerEntityAccess targetPlayerEntityAccess = (ServerPlayerEntityAccess) targetPlayer;
        int successCode = targetPlayerEntityAccess.getEcPlayerData().setNickname(nicknameText);

        //inform command sender that the nickname has been set
        if (successCode >= 0) {
            source.sendFeedback(TextUtil.concat(
                ECText.getInstance().getText("cmd.nickname.set.feedback").setStyle(Config.FORMATTING_DEFAULT),
                (nicknameText != null) ? nicknameText : new LiteralText(targetPlayer.getGameProfile().getName()),
                ECText.getInstance().getText("generic.quote_fullstop").setStyle(Config.FORMATTING_DEFAULT)
            ), Config.BROADCAST_TO_OPS);
        } else {
            MutableText failReason = switch (successCode) {
                case -1 -> ECText.getInstance().getText("cmd.nickname.set.error.perms");
                case -2 -> ECText.getInstance().getText(
                    "cmd.nickname.set.error.length",
                    nicknameText.getString().length(),
                    Config.NICKNAME_MAX_LENGTH
                );
                default -> ECText.getInstance().getText("generic.error.unknown");
            };
            source.sendError(TextUtil.concat(
                ECText.getInstance().getText("cmd.nickname.set.error.1").setStyle(Config.FORMATTING_ERROR),
                nicknameText,
                ECText.getInstance().getText("cmd.nickname.set.error.2").setStyle(Config.FORMATTING_ERROR),
                failReason.setStyle(Config.FORMATTING_ERROR)
            ));
        }

        return successCode;
    }

}
