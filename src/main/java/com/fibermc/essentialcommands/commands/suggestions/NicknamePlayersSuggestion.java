package com.fibermc.essentialcommands.commands.suggestions;

import java.util.Optional;

import com.fibermc.essentialcommands.playerdata.PlayerData;
import com.fibermc.essentialcommands.playerdata.PlayerDataManager;

import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.MutableComponent;

public final class NicknamePlayersSuggestion {
    private NicknamePlayersSuggestion() {}

    //Brigader Suggestions
    public static final SuggestionProvider<CommandSourceStack> STRING_SUGGESTIONS_PROVIDER =
        ListSuggestion.ofContext(context ->
            PlayerDataManager.getInstance().getAllPlayerData().stream()
                .map(PlayerData::getNickname)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(MutableComponent::getString)
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList()
        );
}
