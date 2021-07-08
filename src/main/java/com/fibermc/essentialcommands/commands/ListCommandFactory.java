package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.Config;
import com.fibermc.essentialcommands.commands.suggestions.SuggestionListProvider;
import com.fibermc.essentialcommands.types.MinecraftLocation;
import com.fibermc.essentialcommands.util.TextUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static com.fibermc.essentialcommands.util.TextUtil.clickableTeleport;

public class ListCommandFactory {
    // Specify leading response text, and supplier of list of strings/Text
    public static Command<ServerCommandSource> create(String responsePreText, String commandExecText, SuggestionListProvider<Entry<String, MinecraftLocation>> suggestionsProvider) {
        return (CommandContext<ServerCommandSource> context) -> {
            MutableText responseText = new LiteralText("");
            responseText.append(new LiteralText(responsePreText).setStyle(Config.FORMATTING_DEFAULT));
            Collection<Entry<String, MinecraftLocation>> suggestionsList = suggestionsProvider.getSuggestionList(context);

            List<Text> suggestionTextList = suggestionsList.stream().map((entry) -> clickableTeleport(
                new LiteralText(entry.getKey()).setStyle(Config.FORMATTING_ACCENT),
                entry.getKey(),
                String.format("/%s", commandExecText))
            ).collect(Collectors.toList());
            
            responseText.append(TextUtil.join(
                suggestionTextList,
                new LiteralText(", ").setStyle(Config.FORMATTING_DEFAULT)
            ));
            context.getSource().sendFeedback(
                responseText,
                Config.BROADCAST_TO_OPS
            );
            return 0;
        };
    }
}
