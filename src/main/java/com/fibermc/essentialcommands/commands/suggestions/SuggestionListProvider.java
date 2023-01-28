package com.fibermc.essentialcommands.commands.suggestions;

import java.util.Collection;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.server.command.ServerCommandSource;

@FunctionalInterface
public interface SuggestionListProvider<T> {
    Collection<T> getSuggestionList(CommandContext<ServerCommandSource> context) throws CommandSyntaxException;
}
