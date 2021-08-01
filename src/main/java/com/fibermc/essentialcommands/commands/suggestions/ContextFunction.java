package com.fibermc.essentialcommands.commands.suggestions;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

@FunctionalInterface
public interface ContextFunction<T, R> {
    R apply(T o) throws CommandSyntaxException;
}
