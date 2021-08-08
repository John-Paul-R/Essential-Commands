package dev.jpcode.eccore.util;

import net.minecraft.text.Text;

@FunctionalInterface
public interface StringToTextParser {

    Text parseText(String str);
}
