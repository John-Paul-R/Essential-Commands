package dev.jpcode.eccore.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.mojang.serialization.JsonOps;

import net.minecraft.server.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Style;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("TextUtil")
public class TextUtilTests {
    static {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        BuiltInRegistries.bootStrap();
    }

    @Test
    @DisplayName("flattenRoot output is shaped correctly")
    void flattenRoot_flattensCorrectly()
    {
        var baseStyle = Style.EMPTY.withColor(ChatFormatting.AQUA);
        var input = Component.literal("testing").setStyle(baseStyle)
            .append("token2")
            .append("token3");

        var output = TextUtil.flattenRoot(input);

        assertEquals(output.getFirst().getContents(), input.getContents());
        assertEquals(output.getFirst().getStyle(), baseStyle);

        var inputSiblings = input.getSiblings();
        for (int i = 1; i < output.size(); i++) {
            var inputToken = inputSiblings.get(i - 1);
            var outToken = output.get(i);

            assertEquals(inputToken.getContents(), outToken.getContents());
            assertEquals(inputToken.getStyle(), outToken.getStyle());
        }
    }

    @Test
    @DisplayName("from-to json is remotely sane")
    void fromToJson_isSane()
    {
        var originalText = Component.literal(" hi there! ");
        var textAsJson = ComponentSerialization.CODEC.encodeStart(JsonOps.INSTANCE, originalText)
            .getOrThrow();
        var parsedText = ComponentSerialization.CODEC.parse(JsonOps.INSTANCE, textAsJson)
            .getOrThrow();
        assertEquals(originalText.getContents(), parsedText.getContents());
        assertEquals(originalText.getStyle(), parsedText.getStyle()); // I think both null?
    }
}
