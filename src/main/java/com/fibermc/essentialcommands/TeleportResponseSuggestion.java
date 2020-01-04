package com.fibermc.essentialcommands;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.server.command.ServerCommandSource;

import java.util.stream.Collectors;

public class TeleportResponseSuggestion {

    //Brigader Suggestions
    public static SuggestionProvider<ServerCommandSource> suggestedStrings(PlayerDataManager dataManager) {
        return (context, builder) -> ListSuggestion.getSuggestionsBuilder(builder,
                dataManager.getOrCreate(
                        context.getSource().getPlayer()).getTpAskers()
                        .stream().map((entry) -> entry.getPlayer().getGameProfile().getName())
                        .collect(Collectors.toList()));
    }
}
