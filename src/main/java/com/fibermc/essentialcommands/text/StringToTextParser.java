package com.fibermc.essentialcommands.text;

import net.minecraft.text.Text;

@FunctionalInterface
public interface StringToTextParser {

    Text parseText(String str);
}
