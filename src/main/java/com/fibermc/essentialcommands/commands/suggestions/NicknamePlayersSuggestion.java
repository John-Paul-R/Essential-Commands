package com.fibermc.essentialcommands.commands.suggestions;

import java.util.Optional;

import com.fibermc.essentialcommands.PlayerData;
import com.fibermc.essentialcommands.PlayerDataManager;

import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.MutableText;

public final class NicknamePlayersSuggestion {
    private NicknamePlayersSuggestion() {}

    //Brigader Suggestions
    public static final SuggestionProvider<ServerCommandSource> STRING_SUGGESTIONS_PROVIDER =
        ListSuggestion.ofContext(context ->
            PlayerDataManager.getInstance().getAllPlayerData().stream()
                .map(PlayerData::getNickname)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(MutableText::getString)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList()
        );
}
