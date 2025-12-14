package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.EssentialCommands;
import com.fibermc.essentialcommands.playerdata.PlayerData;
import com.fibermc.essentialcommands.text.ECText;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import dev.jpcode.eccore.util.TextUtil;

public class ModInfoCommand implements Command<CommandSourceStack> {

    private final String modVersion = EssentialCommands.MOD_METADATA.getVersion().getFriendlyString();

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var senderPlayer = context.getSource().getPlayerOrException();
        var ecText = ECText.access(senderPlayer);
        PlayerData.access(senderPlayer).sendCommandFeedback(TextUtil.concat(
            ecText.literal(EssentialCommands.MOD_METADATA.getName()),
            Component.literal(" "),
            ecText.accent(modVersion)
        ));

        return 0;
    }
}
