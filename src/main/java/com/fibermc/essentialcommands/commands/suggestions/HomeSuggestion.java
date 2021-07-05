package com.fibermc.essentialcommands.commands.suggestions;

import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.server.command.ServerCommandSource;

import java.util.ArrayList;
import java.util.List;

public class HomeSuggestion {
    //Brigader Suggestions
    public static SuggestionProvider<ServerCommandSource> suggestedStrings() {
        return (context, builder) -> ListSuggestion.getSuggestionsBuilder(builder,
            getSuggestionsList(context));
    }

    public static List<String> getSuggestionsList(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return new ArrayList<>(((ServerPlayerEntityAccess)context.getSource().getPlayer()).getEcPlayerData().getHomeNames());
    }
}
