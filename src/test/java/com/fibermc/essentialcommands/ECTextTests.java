package com.fibermc.essentialcommands;

import java.util.NoSuchElementException;

import com.fibermc.essentialcommands.text.ECText;
import com.fibermc.essentialcommands.text.TextFormatType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("ECText")
public class ECTextTests {
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
    @DisplayName("getTextInternal - no interplolation")
    void getTextInternal_FormatsCorrectly()
    {
        var expected = ECText.unstyled("enabled").setStyle(TextFormatType.Default.getStyle());
        var enabledText = ecText.getText("generic.enabled");

        assertEquals(enabledText.getContent(), expected.getContent());
        assertEquals(enabledText.getStyle(), expected.getStyle());
    }

    @Test
    @DisplayName("getTextInternal - two interpolated tokens")
    void getTextInternal_TwoInterpolatedTokens_FormatsCorrectly()
    {
        var playerNameText = ECText.unstyled("Steve").formatted(Formatting.AQUA);
        var defaultStyle = TextFormatType.Default.getStyle();
        var accentStyle = TextFormatType.Accent.getStyle();
        var expectedMessage = Text.empty()
            .append(ECText.unstyled("Flight ").setStyle(defaultStyle))
            .append(ECText.unstyled("enabled").setStyle(accentStyle))
            .append(ECText.unstyled(" for ").setStyle(defaultStyle))
            .append(playerNameText)
            .append(ECText.unstyled(".").setStyle(defaultStyle));

        var enabledText = ecText.getText("generic.enabled").setStyle(accentStyle);

        var actualMessage = ecText.getText("cmd.fly.feedback", enabledText, playerNameText);

        var expectedString = expectedMessage.getString();
        var actualString = actualMessage.getString();

        assertEquals(expectedString, actualString);

        var expectedSiblings = expectedMessage.getSiblings();
        var actualSiblings = actualMessage.getSiblings();
        for (int i = 0; i < expectedSiblings.size(); i++) {
            var inputToken = expectedSiblings.get(i);
            var actualToken = actualSiblings.get(i);

            assertEquals(inputToken.getContent(), actualToken.getContent());
            assertEquals(inputToken.getStyle(), actualToken.getStyle());
        }
    }

    @Test
    @DisplayName("getTextInternal - first token interpolated")
    void getTextInternal_FirstTokenInterpolated_FormatsCorrectly()
    {
        var playerNameText = Text.empty()
            .append(ECText.unstyled("[UnstyledPrefix] "))
            .append(ECText.unstyled("Steve").formatted(Formatting.AQUA));
        var defaultStyle = TextFormatType.Default.getStyle();
        var expectedMessage = Text.empty()
            .append(playerNameText)
            .append(ECText.unstyled(" is now AFK.").setStyle(defaultStyle));

        var actualMessage = ecText.getText("player.afk.enter", playerNameText);

        var expectedString = expectedMessage.getString();
        var actualString = actualMessage.getString();

        assertEquals(expectedString, actualString);

        var expectedSiblings = expectedMessage.getSiblings();
        var actualSiblings = actualMessage.getSiblings();
        for (int i = 0; i < expectedSiblings.size(); i++) {
            var inputToken = expectedSiblings.get(i);
            var actualToken = actualSiblings.get(i);

            assertEquals(inputToken.getContent(), actualToken.getContent());
            assertEquals(inputToken.getStyle(), actualToken.getStyle());
        }
    }
}
