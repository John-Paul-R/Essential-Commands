package dev.jpcode.eccore.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

@DisplayName("TextUtil")
public class TextUtilTests
{
    @Test
    @DisplayName("flattenRoot output is shaped correctly")
    void flattenRoot_flattensCorrectly()
    {
        var baseStyle = Style.EMPTY.withColor(Formatting.AQUA);
        var input = Text.literal("testing").setStyle(baseStyle)
            .append("token2")
            .append("token3");

        var output = TextUtil.flattenRoot(input);

        assert output.get(0).getContent().equals(input.getContent());
        assert output.get(0).getStyle().equals(baseStyle);

        var inputSiblings = input.getSiblings();
        for (int i = 1; i < output.size(); i++) {
            var inputToken = inputSiblings.get(i - 1);
            var outToken = output.get(i);

            assert inputToken.getContent().equals(outToken.getContent());
            assert inputToken.getStyle().equals(outToken.getStyle());
        }
    }
}
