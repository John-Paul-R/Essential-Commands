package com.fibermc.essentialcommands.commands.suggestions;

import com.fibermc.essentialcommands.access.PlayerEntityAccess;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.server.command.ServerCommandSource;

import java.util.ArrayList;

public class HomeSuggestion {
    //Brigader Suggestions
    public static SuggestionProvider<ServerCommandSource> suggestedStrings() {
        return (context, builder) -> ListSuggestion.getSuggestionsBuilder(builder,
                new ArrayList<String>(((PlayerEntityAccess)context.getSource().getPlayer()).getEcPlayerData().getHomeNames()));
    }
}
