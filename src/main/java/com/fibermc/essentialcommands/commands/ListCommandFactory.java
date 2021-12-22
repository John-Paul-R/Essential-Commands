package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.ECText;
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

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;
import static com.fibermc.essentialcommands.util.TextUtil.clickableTeleport;

public class ListCommandFactory {
    // Specify leading response text, and supplier of list of strings/Text
    public static <T> Command<ServerCommandSource> create(String responsePreText, String commandExecText, SuggestionListProvider<Entry<String, T>> suggestionsProvider) {
        return (CommandContext<ServerCommandSource> context) -> {
            MutableText responseText = new LiteralText("");
            responseText.append(new LiteralText(responsePreText).setStyle(CONFIG.FORMATTING_DEFAULT.getValue()));
            Collection<Entry<String, T>> suggestionsList = suggestionsProvider.getSuggestionList(context);

            List<Text> suggestionTextList = suggestionsList.stream().map((entry) -> clickableTeleport(
                new LiteralText(entry.getKey()).setStyle(CONFIG.FORMATTING_ACCENT.getValue()),
                entry.getKey(),
                String.format("/%s", commandExecText))
            ).collect(Collectors.toList());

            if (suggestionTextList.size() > 0) {
                responseText.append(TextUtil.join(
                    suggestionTextList,
                    new LiteralText(", ").setStyle(CONFIG.FORMATTING_DEFAULT.getValue())
                ));
            } else {
                responseText.append(ECText.getInstance().getText("cmd.list.feedback.empty").setStyle(CONFIG.FORMATTING_ERROR.getValue()));
            }
            context.getSource().sendFeedback(
                responseText,
                CONFIG.BROADCAST_TO_OPS.getValue()
            );
            return 0;
        };
    }
}
