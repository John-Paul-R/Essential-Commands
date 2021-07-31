package com.fibermc.essentialcommands.config;

@FunctionalInterface
public interface ValueParser<T> {
    T parseValue(String value);
}
