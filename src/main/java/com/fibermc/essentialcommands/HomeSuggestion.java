package com.fibermc.essentialcommands;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.server.command.ServerCommandSource;

import java.util.ArrayList;

public class HomeSuggestion {
    //Brigader Suggestions
    public static SuggestionProvider<ServerCommandSource> suggestedStrings(PlayerDataManager dataManager) {
        return (context, builder) -> ListSuggestion.getSuggestionsBuilder(builder,
                new ArrayList<String>(dataManager.getOrCreate(context.getSource().getPlayer()).getHomeNames()));
    }
}
