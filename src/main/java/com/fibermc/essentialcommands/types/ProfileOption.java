package com.fibermc.essentialcommands.types;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.commands.CommandSourceStack;

public record ProfileOption<T>(
    ArgumentType<T> argumentType,
    T defaultValue,
    ProfileOptionFromContextSetter<T> profileSetter,
    ProfileOptionGetter<Optional<T>> profileGetter,
    @Nullable SuggestionProvider<CommandSourceStack> suggestionProvider)
{
    public ProfileOption(
        ArgumentType<T> argumentType,
        T defaultValue,
        ProfileOptionFromContextSetter<T> profileSetter,
        ProfileOptionGetter<Optional<T>> profileGetter)
    {
        this(argumentType, defaultValue, profileSetter, profileGetter, null);
    }
}
