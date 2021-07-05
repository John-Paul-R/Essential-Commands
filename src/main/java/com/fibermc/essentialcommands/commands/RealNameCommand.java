package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.Config;
import com.fibermc.essentialcommands.PlayerData;
import com.fibermc.essentialcommands.PlayerDataManager;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;

import java.util.List;
import java.util.UUID;

public class RealNameCommand implements Command<ServerCommandSource> {
    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {

        String nicknameStr = StringArgumentType.getString(context, "player_nickname");
        List<PlayerData> nicknamePlayers = PlayerDataManager.getInstance().getPlayerDataMatchingNickname(nicknameStr);
        MutableText responseText = new LiteralText("");

        // If no players matched the provided nickname
        if (nicknamePlayers.size() == 0) {
            responseText
                .append(new LiteralText("No online players match the nickname '").setStyle(Config.FORMATTING_DEFAULT))
                .append(new LiteralText(nicknameStr).setStyle(Config.FORMATTING_ACCENT))
                .append(new LiteralText("'.").setStyle(Config.FORMATTING_DEFAULT));

        } else {
            responseText
                .append(new LiteralText("The following player(s) match the nickname '").setStyle(Config.FORMATTING_DEFAULT))
                .append(new LiteralText(nicknameStr).setStyle(Config.FORMATTING_ACCENT))
                .append(new LiteralText("':").setStyle(Config.FORMATTING_DEFAULT));

            for (PlayerData nicknamePlayer : nicknamePlayers) {
                responseText.append("\n  ");
                responseText.append(nicknamePlayer.getPlayer().getGameProfile().getName());
            }

        }
        context.getSource().sendFeedback(
            responseText, false
        );

        return 0;
    }
}
