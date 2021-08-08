package com.fibermc.essentialcommands.commands.suggestions;

import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.server.command.ServerCommandSource;
import org.apache.logging.log4j.core.jmx.Server;

import java.util.stream.Collectors;

public class TeleportResponseSuggestion {

    //Brigader Suggestions
    public static SuggestionProvider<ServerCommandSource> suggestedStrings() {
        return ListSuggestion.ofContext((CommandContext<ServerCommandSource> context) ->
            ((ServerPlayerEntityAccess)context.getSource().getPlayer()).getEcPlayerData().getIncomingTeleportRequests().values()
                .stream().map((entry) -> entry.getSenderPlayer().getGameProfile().getName())
                .collect(Collectors.toList())
        );
    }
}
