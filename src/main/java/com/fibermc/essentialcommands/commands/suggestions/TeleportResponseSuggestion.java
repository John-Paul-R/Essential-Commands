package com.fibermc.essentialcommands.commands.suggestions;

import java.util.stream.Collectors;

import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.commands.CommandSourceStack;

public final class TeleportResponseSuggestion {
    private TeleportResponseSuggestion() {}

    //Brigader Suggestions
    public static final SuggestionProvider<CommandSourceStack> STRING_SUGGESTIONS_PROVIDER
        = ListSuggestion.ofContext((CommandContext<CommandSourceStack> context) ->
        ((ServerPlayerEntityAccess) context.getSource().getPlayer()).ec$getPlayerData().getIncomingTeleportRequests().values()
            .stream().map((entry) -> entry.getSenderPlayer().getGameProfile().name())
            .collect(Collectors.toList())
    );
}
