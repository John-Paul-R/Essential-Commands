package com.fibermc.essentialcommands.commands.suggestions;

import java.util.stream.Collectors;

import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.server.command.ServerCommandSource;

public final class TeleportResponseSuggestion {
    private TeleportResponseSuggestion() {}

    //Brigader Suggestions
    public static final SuggestionProvider<ServerCommandSource> STRING_SUGGESTIONS_PROVIDER
        = ListSuggestion.ofContext((CommandContext<ServerCommandSource> context) ->
        ((ServerPlayerEntityAccess) context.getSource().getPlayer()).getEcPlayerData().getIncomingTeleportRequests().values()
            .stream().map((entry) -> entry.getSenderPlayer().getGameProfile().getName())
            .collect(Collectors.toList())
    );
}
