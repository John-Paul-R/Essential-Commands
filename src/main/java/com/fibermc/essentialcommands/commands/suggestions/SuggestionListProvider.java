package com.fibermc.essentialcommands.commands.suggestions;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;

import java.util.Collection;

@FunctionalInterface
public interface SuggestionListProvider<T> {
    Collection<T> getSuggestionList(final CommandContext<ServerCommandSource> context) throws CommandSyntaxException;
}
