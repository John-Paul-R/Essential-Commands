package com.fibermc.essentialcommands.commands.exceptions;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;

public class ECExceptions {
    private static final DynamicCommandExceptionType KEY_EXISTS = new DynamicCommandExceptionType((value) -> new LiteralMessage(String.format("The key '%s' already has an associated location.", value)));

    public static DynamicCommandExceptionType keyExists() {
        return KEY_EXISTS;
    }
}
