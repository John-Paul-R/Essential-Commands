package com.fibermc.essentialcommands.text;

import net.minecraft.network.chat.Component;

@FunctionalInterface
public interface StringToTextParser {

    Component parseText(String str);
}
