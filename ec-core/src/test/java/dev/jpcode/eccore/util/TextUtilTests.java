package dev.jpcode.eccore.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.minecraft.text.Style;
import net.minecraft.util.Formatting;

import static org.gradle.internal.impldep.org.junit.Assert.assertEquals;

@DisplayName("TextUtil")
public class TextUtilTests {
    @Test
    @DisplayName("flattenRoot output is shaped correctly")
    void flattenRoot_flattensCorrectly()
    {
        var baseStyle = Style.EMPTY.withColor(Formatting.AQUA);
        var input = ECText.unstyled("testing").setStyle(baseStyle)
            .append("token2")
            .append("token3");

        var output = TextUtil.flattenRoot(input);

        assertEquals(output.get(0).getContent(), input.getContent());
        assertEquals(output.get(0).getStyle(), baseStyle);

        var inputSiblings = input.getSiblings();
        for (int i = 1; i < output.size(); i++) {
            var inputToken = inputSiblings.get(i - 1);
            var outToken = output.get(i);

            assertEquals(inputToken.getContent(), outToken.getContent());
            assertEquals(inputToken.getStyle(), outToken.getStyle());
        }
    }
}
