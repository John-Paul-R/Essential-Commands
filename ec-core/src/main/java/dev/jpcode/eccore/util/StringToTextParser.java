package dev.jpcode.eccore.util;

import net.minecraft.network.chat.Component;

@FunctionalInterface
public interface StringToTextParser {

    Component parseText(String str);
}
