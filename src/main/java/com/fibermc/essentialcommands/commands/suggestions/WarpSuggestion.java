package com.fibermc.essentialcommands.commands.suggestions;

import com.fibermc.essentialcommands.ManagerLocator;
import com.fibermc.essentialcommands.types.WarpLocation;

import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.commands.CommandSourceStack;

public final class WarpSuggestion {
    private WarpSuggestion() {}

    //Brigader Suggestions
    public static final SuggestionProvider<CommandSourceStack> STRING_SUGGESTIONS_PROVIDER
        = ListSuggestion.ofContext(
            (ctx) -> ManagerLocator.getInstance()
                .getWorldDataManager()
                .getAccessibleWarps(ctx.getSource().getPlayerOrException())
                .map(WarpLocation::getName)
                .toList()
        );

}
