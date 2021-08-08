package dev.jpcode.eccore.config;

@FunctionalInterface
public interface ValueParser<T> {
    T parseValue(String value);
}
