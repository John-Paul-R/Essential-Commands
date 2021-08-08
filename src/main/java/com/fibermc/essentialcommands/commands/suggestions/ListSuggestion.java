package com.fibermc.essentialcommands.commands.suggestions;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class ListSuggestion {
    public static CompletableFuture<Suggestions> buildSuggestions(SuggestionsBuilder builder, Collection<String> suggestionCollection) {
        String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);

        if(suggestionCollection.isEmpty()) { // If the list is empty then return no suggestions
            return Suggestions.empty(); // No suggestions
        }

        for (String str : suggestionCollection) { // Iterate through the supplied list
            if (str.toLowerCase(Locale.ROOT).startsWith(remaining)) {
                builder.suggest(str); // Add every single entry to suggestions list.
            }
        }
        return builder.buildFuture(); // Create the CompletableFuture containing all the suggestions
    }

    @Contract(pure = true)
    public static @NotNull SuggestionProvider<ServerCommandSource> of(Supplier<Collection<String>> suggestionCollection) {
        return (CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) -> {
            return buildSuggestions(builder, suggestionCollection.get());
        };
    }

    @Contract(pure = true)
    public static <S> @NotNull SuggestionProvider<S> ofContext(ContextFunction<CommandContext<S>, Collection<String>> suggestionCollection) {
        return (CommandContext<S> context, SuggestionsBuilder builder) -> {
            return buildSuggestions(builder, suggestionCollection.apply(context));
        };
    }

}
