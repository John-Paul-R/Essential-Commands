package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.Config;
import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.mojang.brigadier.Command;
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

import java.util.UUID;

public class NicknameSetCommand implements Command<ServerCommandSource>  {
    public NicknameSetCommand() {}

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        //Store command sender
        ServerPlayerEntity senderPlayerEntity = context.getSource().getPlayer();

        ServerPlayerEntity targetPlayer;
        try {
            targetPlayer = EntityArgumentType.getPlayer(context, "target");
        } catch (IllegalArgumentException e) {
            targetPlayer = senderPlayerEntity;
        }

        //Get specified new nickname
        Text nickname = TextArgumentType.getTextArgument(context, "nickname");
        MutableText nicknameText = null;
        // If nickname is not null and not empty string
        if ( nickname != null && !"".equals(nickname.getString()) ) {
            nicknameText = Texts.parse(context.getSource(), nickname, senderPlayerEntity, 0);
        }

        ServerPlayerEntityAccess targetPlayerEntityAccess = (ServerPlayerEntityAccess) targetPlayer;
        int successCode = targetPlayerEntityAccess.getEcPlayerData().setNickname(nicknameText);

        //inform command sender that the nickname has been set
        if (successCode >= 0) {
            senderPlayerEntity.sendSystemMessage(
                new LiteralText("")
                    .append(new LiteralText("Nickname set to '").setStyle(Config.FORMATTING_DEFAULT))
                    .append(
                        (nicknameText != null) ?
                            nicknameText : new LiteralText(senderPlayerEntity.getGameProfile().getName())
                    ).append(new LiteralText("'.").setStyle(Config.FORMATTING_DEFAULT))
                , new UUID(0, 0));
        } else {
            String failReason;
            switch (successCode) {
                case -1:
                    failReason = "Player has insufficient permissions for specified nickname.";
                    break;
                case -2:
                    failReason = String.format(
                        "Length of supplied nickname (%s) exceeded max nickname length (%s)",
                        nicknameText.getString().length(),
                        Config.NICKNAME_MAX_LENGTH
                    );
                    break;
                default:
                    failReason = "Unknown";
                    break;
            }
            senderPlayerEntity.sendSystemMessage(
                new LiteralText("Nickname could not be set to '").setStyle(Config.FORMATTING_ERROR)
                    .append(nicknameText)
                    .append(new LiteralText("'. Reason: ").setStyle(Config.FORMATTING_ERROR))
                    .append(new LiteralText(failReason).setStyle(Config.FORMATTING_ERROR))
//                    .append(new LiteralText(String.valueOf(Config.HOME_LIMIT)).setStyle(Config.FORMATTING_ACCENT))
//                    .append(new LiteralText(") reached.").setStyle(Config.FORMATTING_ERROR))
                , new UUID(0, 0));
        }

        return successCode;
    }

}
