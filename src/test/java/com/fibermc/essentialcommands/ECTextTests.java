package com.fibermc.essentialcommands;

import java.util.NoSuchElementException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

@DisplayName("ECText")
public class ECTextTests
{
    private static ECText ecText;

    @BeforeAll
    public static void setup() {
        try {
            ECText.init(null);
            ecText = ECText.getInstance();
        } catch (NoSuchElementException ignored) {
            // We don't care about this. Is a startup error in EssentialCommands.java relating to loading ModMetadata.
        }
    }

    @Test
    @DisplayName("getTextInternal - no interplolation - success")
    void getTextInternal_FormatsCorrectly()
    {
        var expected = Text.literal("enabled").setStyle(TextFormatType.Default.getStyle());
        var enabledText = ecText.getText("generic.enabled");

        assert enabledText.getContent().equals(expected.getContent());
        assert enabledText.getStyle().equals(expected.getStyle());
    }

    @Test
    @DisplayName("getTextInternal - single interplolation - success")
    void getTextInternal_SingleIterpolatedToken_FormatsCorrectly()
    {
        var playerNameText = Text.literal("Steve").formatted(Formatting.AQUA);
        var defaultStyle = TextFormatType.Default.getStyle();
        var accentStyle = TextFormatType.Accent.getStyle();
        var expectedMessage = Text.empty()
            .append(Text.literal("Flight ").setStyle(defaultStyle))
            .append(Text.literal("enabled").setStyle(accentStyle))
            .append(Text.literal(" for ").setStyle(defaultStyle))
            .append(playerNameText)
            .append(Text.literal(".").setStyle(defaultStyle));

        var enabledText = ecText.getText("generic.enabled").setStyle(accentStyle);

        var actualMessage = ecText.getText("cmd.fly.feedback", enabledText, playerNameText);

        var expectedString = expectedMessage.getString();
        var actualString = actualMessage.getString();

        assert expectedString.equals(actualString);

        var expectedSiblings = expectedMessage.getSiblings();
        var actualSiblings = actualMessage.getSiblings();
        for (int i = 0; i < expectedSiblings.size(); i++) {
            var inputToken = expectedSiblings.get(i);
            var actualToken = actualSiblings.get(i);

            assert inputToken.getContent().equals(actualToken.getContent());
            assert inputToken.getStyle().equals(actualToken.getStyle());
        }
    }
}
