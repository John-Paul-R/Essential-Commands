package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.ECText;
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
            MutableText responseText = Text.literal("");
            responseText.append(Text.literal(responsePreText).setStyle(CONFIG.FORMATTING_DEFAULT.getValue()));
            Collection<Entry<String, T>> suggestionsList = suggestionsProvider.getSuggestionList(context);

            List<Text> suggestionTextList = suggestionsList.stream().map((entry) -> clickableTeleport(
                Text.literal(entry.getKey()).setStyle(CONFIG.FORMATTING_ACCENT.getValue()),
                entry.getKey(),
                String.format("/%s", commandExecText))
            ).collect(Collectors.toList());

            if (suggestionTextList.size() > 0) {
                responseText.append(TextUtil.join(
                    suggestionTextList,
                    Text.literal(", ").setStyle(CONFIG.FORMATTING_DEFAULT.getValue())
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
