package com.fibermc.essentialcommands.commands.suggestions;

import com.fibermc.essentialcommands.ManagerLocator;
import com.fibermc.essentialcommands.access.PlayerEntityAccess;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.server.command.ServerCommandSource;

import java.util.stream.Collectors;

public class TeleportResponseSuggestion {

    //Brigader Suggestions
    public static SuggestionProvider<ServerCommandSource> suggestedStrings() {
        return (context, builder) -> ListSuggestion.getSuggestionsBuilder(builder,
            ((PlayerEntityAccess)context.getSource().getPlayer()).getEcPlayerData().getTpAskers()
                .stream().map((entry) -> entry.getPlayer().getGameProfile().getName())
                .collect(Collectors.toList())
        );
    }
}
