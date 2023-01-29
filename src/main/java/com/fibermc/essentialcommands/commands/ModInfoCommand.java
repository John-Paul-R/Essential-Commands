package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.EssentialCommands;
import com.fibermc.essentialcommands.playerdata.PlayerData;
import com.fibermc.essentialcommands.text.ECText;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.server.command.ServerCommandSource;

import dev.jpcode.eccore.util.TextUtil;

public class ModInfoCommand implements Command<ServerCommandSource> {

    private final String modVersion = EssentialCommands.MOD_METADATA.getVersion().getFriendlyString();

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var senderPlayer = context.getSource().getPlayer();
        var ecText = ECText.access(senderPlayer);
        PlayerData.access(senderPlayer).sendCommandFeedback(TextUtil.concat(
            ecText.literal(EssentialCommands.MOD_METADATA.getName()),
            TextUtil.literal(" "),
            ecText.accent(modVersion)
        ));

        return 0;
    }
}
