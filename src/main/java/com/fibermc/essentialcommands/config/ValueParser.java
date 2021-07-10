package com.fibermc.essentialcommands.config;

@FunctionalInterface
public interface ValueParser<T> {
    public T parseValue(String value);
}
