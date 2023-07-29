package com.fibermc.essentialcommands.commands.suggestions;

import com.fibermc.essentialcommands.ManagerLocator;
import com.fibermc.essentialcommands.types.WarpLocation;

import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.server.command.ServerCommandSource;

public final class WarpSuggestion {
    private WarpSuggestion() {}

    //Brigader Suggestions
    public static final SuggestionProvider<ServerCommandSource> STRING_SUGGESTIONS_PROVIDER
        = ListSuggestion.ofContext(
            (ctx) -> ManagerLocator.getInstance()
                .getWorldDataManager()
                .getAccessibleWarps(ctx.getSource().getPlayerOrThrow())
                .map(WarpLocation::getName)
                .toList()
        );

}
