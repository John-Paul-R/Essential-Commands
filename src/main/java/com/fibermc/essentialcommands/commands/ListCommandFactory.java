package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.Config;
import com.fibermc.essentialcommands.commands.suggestions.SuggestedStringsListProvider;
import com.fibermc.essentialcommands.util.TextUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;

import java.util.List;
import java.util.UUID;

public class ListCommandFactory {
    // Specify leading response text, and supplier of list of strings/Text
    public static Command<ServerCommandSource> create(String responsePreText, SuggestedStringsListProvider suggestionsProvider) {
        return (CommandContext<ServerCommandSource> context) -> {
            MutableText responseText = new LiteralText("");
            responseText.append(new LiteralText(responsePreText).setStyle(Config.FORMATTING_DEFAULT));
            List<String> suggestionsList = suggestionsProvider.getSuggestionsList(context);
            responseText.append(TextUtil.joinStrings(
                suggestionsList,
                new LiteralText(", ").setStyle(Config.FORMATTING_DEFAULT),
                Config.FORMATTING_ACCENT
            ));
            context.getSource().sendFeedback(
                responseText,
                Config.BROADCAST_TO_OPS
            );
            return 0;
        };
    }
}
