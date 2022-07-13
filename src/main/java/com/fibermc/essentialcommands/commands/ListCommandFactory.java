package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.ECText;
import com.fibermc.essentialcommands.TextFormatType;
import com.fibermc.essentialcommands.commands.suggestions.SuggestionListProvider;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import dev.jpcode.eccore.util.TextUtil;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;
import static dev.jpcode.eccore.util.TextUtil.clickableTeleport;

public class ListCommandFactory {
    // Specify leading response text, and supplier of list of strings/Text
    public static <T> Command<ServerCommandSource> create(
        String responsePreText,
        String commandExecText,
        SuggestionListProvider<Entry<String, T>> suggestionsProvider)
    {
        return (CommandContext<ServerCommandSource> context) -> {
            MutableText responseText = Text.empty()
                .append(ECText.literal(responsePreText));

            Collection<Entry<String, T>> suggestionsList = suggestionsProvider.getSuggestionList(context);

            List<Text> suggestionTextList = suggestionsList.stream()
                .map((entry) -> clickableTeleport(
                    ECText.accent(entry.getKey()),
                    entry.getKey(),
                    String.format("/%s", commandExecText)
                ))
                .collect(Collectors.toList());

            if (suggestionTextList.size() > 0) {
                responseText.append(TextUtil.join(
                    suggestionTextList,
                    ECText.literal(", ")
                ));
            } else {
                responseText.append(ECText.getInstance().getText("cmd.list.feedback.empty", TextFormatType.Error));
            }
            context.getSource().sendFeedback(
                responseText,
                CONFIG.BROADCAST_TO_OPS
            );
            return 0;
        };
    }
}
