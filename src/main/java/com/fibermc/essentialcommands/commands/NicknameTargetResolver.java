package com.fibermc.essentialcommands.commands;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.fibermc.essentialcommands.access.EntitySelectorNicknameAccess;
import com.fibermc.essentialcommands.playerdata.PlayerData;
import com.fibermc.essentialcommands.playerdata.PlayerDataManager;
import com.fibermc.essentialcommands.types.NicknameCommandArgMode;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;

/**
 * Adds optional nickname fallback on top of Minecraft's normal player
 * argument handling. Vanilla resolution is always attempted first.
 */
public final class NicknameTargetResolver {
    private NicknameTargetResolver() {}

    public static RequiredArgumentBuilder<CommandSourceStack, EntitySelector> targetPlayerArgument() {
        return Commands.argument("target_player", EntityArgument.player())
            .suggests(NicknameTargetResolver::suggestPlayers);
    }

    public static RequiredArgumentBuilder<CommandSourceStack, EntitySelector> targetPlayerArgumentNonGreedy() {
        return targetPlayerArgument();
    }

    private static CompletableFuture<Suggestions> suggestPlayers(
        CommandContext<CommandSourceStack> context,
        SuggestionsBuilder builder
    ) throws CommandSyntaxException {
        if (CONFIG.NICKNAMES_AS_COMMAND_ARG != NicknameCommandArgMode.Never) {
            addNicknameSuggestions(builder);
        }

        return EntityArgument.player().listSuggestions(context, builder);
    }

    public static void addNicknameSuggestions(SuggestionsBuilder builder) {
        if (!PlayerDataManager.exists()) {
            return;
        }

        String remaining = PlayerData.normalizeNickname(builder.getRemaining());

        for (PlayerData playerData : PlayerDataManager.getInstance().getAllPlayerData()) {
            String normalized = playerData.getNormalizedNickname();
            if (normalized == null || !normalized.startsWith(remaining)) {
                continue;
            }
            playerData.getNickname()
                .map(Component::getString)
                .map(nick -> nick.replaceAll("\\s+", ""))
                .filter(nick -> !nick.isBlank())
                .ifPresent(builder::suggest);
        }
    }

    public static ServerPlayer getPlayer(
        CommandContext<CommandSourceStack> context,
        String argumentName
    ) throws CommandSyntaxException {
        try {
            return EntityArgument.getPlayer(context, argumentName);
        } catch (CommandSyntaxException vanillaFailure) {
            if (CONFIG.NICKNAMES_AS_COMMAND_ARG != NicknameCommandArgMode.EssentialCommandsOnly) {
                throw vanillaFailure;
            }

            EntitySelector selector = context.getArgument(argumentName, EntitySelector.class);
            String playerName = ((EntitySelectorNicknameAccess) selector).ec$getPlayerName();
            ServerPlayer nicknameMatch = resolvePlayerByNickname(playerName);
            if (nicknameMatch != null) {
                return nicknameMatch;
            }

            throw vanillaFailure;
        }
    }

    /**
     * Finds one online player whose nickname matches the literal command
     * player name after normalization (whitespace removal + case folding).
     *
     * Returns null for no match or an ambiguous match.
     */
    public static ServerPlayer resolvePlayerByNickname(String playerName) {
        if (
            playerName == null
            || playerName.isBlank()
        ) {
            return null;
        }

        List<PlayerData> matches = PlayerDataManager.getInstance().getByNickname(playerName);
        // Ambiguous (>1) or no match -- never choose one arbitrarily.
        if (matches.size() != 1) {
            return null;
        }
        return matches.getFirst().getPlayer();
    }
}
