package com.fibermc.essentialcommands.commands.suggestions;

import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.server.command.ServerCommandSource;

import java.util.stream.Collectors;

public class TeleportResponseSuggestion {

    //Brigader Suggestions
    public static SuggestionProvider<ServerCommandSource> suggestedStrings() {
        return (context, builder) -> ListSuggestion.getSuggestionsBuilder(builder,
            ((ServerPlayerEntityAccess)context.getSource().getPlayer()).getEcPlayerData().getTpAskers()
                .stream().map((entry) -> entry.getPlayer().getGameProfile().getName())
                .collect(Collectors.toList())
        );
    }
}
