package com.fibermc.essentialcommands.commands;

import java.util.List;

import com.fibermc.essentialcommands.playerdata.PlayerData;
import com.fibermc.essentialcommands.playerdata.PlayerDataManager;
import com.fibermc.essentialcommands.text.ECText;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;

public class RealNameCommand implements Command<CommandSourceStack> {
    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {

        String nicknameStr = StringArgumentType.getString(context, "player_nickname");
        List<PlayerData> nicknamePlayers = PlayerDataManager.getInstance().getPlayerDataMatchingNickname(nicknameStr);
        MutableComponent responseText = Component.empty();

        var ecText = ECText.access(context.getSource().getPlayerOrException());
        var nicknameText = ecText.accent(nicknameStr);
        // If no players matched the provided nickname
        if (nicknamePlayers.size() == 0) {
            responseText
                .append(ecText.getText("cmd.realname.feedback.none_match", nicknameText));

        } else {
            responseText
                .append(ecText.getText("cmd.realname.feedback.matching", nicknameText));

            for (PlayerData nicknamePlayer : nicknamePlayers) {
                responseText.append("\n  ");
                responseText.append(nicknamePlayer.getPlayer().getGameProfile().name());
            }
        }

        context.getSource().sendSuccess(() -> responseText, CONFIG.BROADCAST_TO_OPS);

        return 0;
    }
}
