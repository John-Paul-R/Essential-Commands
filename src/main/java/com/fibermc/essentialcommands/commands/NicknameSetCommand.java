package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.Config;
import com.fibermc.essentialcommands.access.PlayerEntityAccess;
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
import net.minecraft.util.Formatting;

import java.util.Objects;
import java.util.UUID;

public class NicknameSetCommand implements Command<ServerCommandSource>  {
    public NicknameSetCommand() {}

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        //Store command sender
        ServerPlayerEntity senderPlayerEntity = context.getSource().getPlayer();

        ServerPlayerEntity targetPlayer = null;
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

        PlayerEntityAccess targetPlayerEntityAccess = (PlayerEntityAccess) targetPlayer;
        int successCode = targetPlayerEntityAccess.getEcPlayerData().setNickname(nicknameText);

        //inform command sender that the home has been set
        if (successCode == 0) {
            senderPlayerEntity.sendSystemMessage(
                new LiteralText("")
                    .append(new LiteralText("Nickname set to '").setStyle(Config.FORMATTING_DEFAULT))
                    .append(
                        Objects.nonNull(nicknameText) ?
                            nicknameText : new LiteralText(senderPlayerEntity.getGameProfile().getName())
                    ).append(new LiteralText("'.").setStyle(Config.FORMATTING_DEFAULT))
                , new UUID(0, 0));
        } else if (successCode==1) {
            senderPlayerEntity.sendSystemMessage(
                new LiteralText("Nickname could not be set to '").setStyle(Config.FORMATTING_ERROR)
                    .append(nicknameText)
                    .append(new LiteralText("'. Reason: ").setStyle(Config.FORMATTING_ERROR))
//                    .append(new LiteralText(String.valueOf(Config.HOME_LIMIT)).setStyle(Config.FORMATTING_ACCENT))
//                    .append(new LiteralText(") reached.").setStyle(Config.FORMATTING_ERROR))
                , new UUID(0, 0));
        }

        return successCode;
    }

}
