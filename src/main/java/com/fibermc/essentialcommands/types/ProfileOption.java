package com.fibermc.essentialcommands.types;

import com.mojang.brigadier.arguments.ArgumentType;

public record ProfileOption<T>(
    ArgumentType<T> argumentType,
    T defaultValue,
    ProfileOptionFromContextSetter<T> profileSetter,
    ProfileOptionGetter<T> profileGetter) {}
