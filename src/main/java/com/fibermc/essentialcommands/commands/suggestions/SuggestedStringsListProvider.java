package com.fibermc.essentialcommands.commands.suggestions;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;

import java.util.List;

@FunctionalInterface
public interface SuggestedStringsListProvider {
    List<String> getSuggestionsList(final CommandContext<ServerCommandSource> context) throws CommandSyntaxException;
}

