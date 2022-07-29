package com.fibermc.essentialcommands.types;

import com.mojang.brigadier.arguments.ArgumentType;

public record ProfileOption<T>(ArgumentType<T> argumentType, T defaultValue) {

    public ArgumentType<T> getArgumentType() {
        return argumentType;
    }

    public T getDefaultValue() {
        return defaultValue;
    }
}
