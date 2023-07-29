package com.fibermc.essentialcommands.commands;

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fibermc.essentialcommands.commands.suggestions.SuggestionListProvider;
import com.fibermc.essentialcommands.playerdata.PlayerProfile;
import com.fibermc.essentialcommands.text.ECText;
import com.fibermc.essentialcommands.text.TextFormatType;
import com.fibermc.essentialcommands.types.IStyleProvider;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import dev.jpcode.eccore.util.TextUtil;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;
import static dev.jpcode.eccore.util.TextUtil.clickableTeleport;

public final class ListCommandFactory {
    private ListCommandFactory() {}

    // Specify leading response text, and supplier of list of strings/Text
    public static <T> Command<ServerCommandSource> create(
        String responsePreText,
        String commandExecText,
        SuggestionListProvider<Entry<String, T>> suggestionsProvider)
    {
        return (CommandContext<ServerCommandSource> context) -> {
            var styleProvider = PlayerProfile.accessFromContextOrThrow(context);
            Collection<Entry<String, T>> suggestionsList = suggestionsProvider.getSuggestionList(context);

            context.getSource().sendFeedback(() ->
                getSuggestionText(responsePreText, commandExecText, suggestionsList, Entry::getKey, styleProvider),
                CONFIG.BROADCAST_TO_OPS
            );
            return 0;
        };
    }

    public static <T> Command<ServerCommandSource> create(
        String responsePreText,
        String commandExecText,
        SuggestionListProvider<T> suggestionsProvider,
        Function<T, String> nameAccessor)
    {
        return (CommandContext<ServerCommandSource> context) -> {
            var styleProvider = PlayerProfile.accessFromContextOrThrow(context);
            Collection<T> suggestionsList = suggestionsProvider.getSuggestionList(context);

            context.getSource().sendFeedback(() ->
                    getSuggestionText(responsePreText, commandExecText, suggestionsList, nameAccessor, styleProvider),
                CONFIG.BROADCAST_TO_OPS
            );
            return 0;
        };
    }

    public static <T> Text getSuggestionText(
        String responsePreText,
        String commandExecText,
        Collection<T> suggestionsList,
        Function<T, String> nameAccessor,
        IStyleProvider styleProvider)
    {
        MutableText responseText = Text.empty()
            .append(Text.literal(responsePreText).setStyle(styleProvider.getStyle(TextFormatType.Default)));

        List<Text> suggestionTextList = suggestionsList.stream()
            .map(nameAccessor)
            .map((name) -> clickableTeleport(
                Text.literal(name).setStyle(styleProvider.getStyle(TextFormatType.Accent)),
                name,
                String.format("/%s", commandExecText)))
            .collect(Collectors.toList());

        if (suggestionTextList.size() > 0) {
            responseText.append(TextUtil.join(
                suggestionTextList,
                Text.literal(", ").setStyle(styleProvider.getStyle(TextFormatType.Default))
            ));
        } else {
            responseText.append(ECText.getInstance().getText("cmd.list.feedback.empty", TextFormatType.Error, styleProvider));
        }
        return responseText;
    }
}
