package com.fibermc.essentialcommands.commands.suggestions;

import com.fibermc.essentialcommands.ManagerLocator;

import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.server.command.ServerCommandSource;

public final class WarpSuggestion {
    private WarpSuggestion() {}

    //Brigader Suggestions
    public static final SuggestionProvider<ServerCommandSource> STRING_SUGGESTIONS_PROVIDER
        = ListSuggestion.of(() -> ManagerLocator.getInstance().getWorldDataManager().getWarpNames());

}
