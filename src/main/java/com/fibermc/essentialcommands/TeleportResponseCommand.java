package com.fibermc.essentialcommands;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.server.command.ServerCommandSource;

import java.util.stream.Collectors;

public abstract class TeleportResponseCommand {

    PlayerDataManager dataManager;

    TeleportResponseCommand(PlayerDataManager dataManager) {
        this.dataManager = dataManager;
    }

    //Brigader Suggestions
    public SuggestionProvider<ServerCommandSource> suggestedStrings() {
        return (context, builder) -> ListSuggestion.getSuggestionsBuilder(builder,
                dataManager.getOrCreate(
                        context.getSource().getPlayer()).getTpAskers()
                        .stream().map((entry) -> entry.getPlayer().getName().toString())
                        .collect(Collectors.toList()));
    }
}
