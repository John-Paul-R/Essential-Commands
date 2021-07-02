package com.fibermc.essentialcommands.commands.suggestions;

import com.fibermc.essentialcommands.PlayerDataManager;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.server.command.ServerCommandSource;

import java.util.Objects;

public class NicknamePlayersSuggestion {
    //Brigader Suggestions
    public static SuggestionProvider<ServerCommandSource> suggestedStrings() {
        return (context, builder) -> ListSuggestion.getSuggestionsBuilder(builder,
            PlayerDataManager.getInstance().getAllPlayerData().stream()
                .map(playerData -> playerData.getNickname().getString())
                .filter(Objects::nonNull)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList()
        );
    }
}
