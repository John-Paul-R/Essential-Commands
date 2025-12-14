package com.fibermc.essentialcommands;

import java.util.NoSuchElementException;

import com.fibermc.essentialcommands.text.ECText;
import com.fibermc.essentialcommands.text.TextFormatType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import net.minecraft.server.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("ECText")
public class ECTextTests {
    private static ECText ecText;

    static {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        BuiltInRegistries.bootStrap();
    }

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
    @DisplayName("getTextInternal - no interpolation")
    void getTextInternal_FormatsCorrectly()
    {
        var expected = Component.literal("enabled").setStyle(TextFormatType.Default.getStyle());
        var enabledText = ecText.getText("generic.enabled");

        assertEquals(expected.getContents(), enabledText.getContents());
        assertEquals(expected.getStyle(), enabledText.getStyle());
    }

    @Test
    @DisplayName("getTextInternal - two interpolated tokens")
    void getTextInternal_TwoInterpolatedTokens_FormatsCorrectly()
    {
        var playerNameText = Component.literal("Steve").withStyle(ChatFormatting.AQUA);
        var defaultStyle = TextFormatType.Default.getStyle();
        var accentStyle = TextFormatType.Accent.getStyle();
        var expectedMessage = Component.empty()
            .append(Component.literal("Flight ").setStyle(defaultStyle))
            .append(Component.literal("enabled").setStyle(accentStyle))
            .append(Component.literal(" for ").setStyle(defaultStyle))
            .append(playerNameText)
            .append(Component.literal(".").setStyle(defaultStyle));

        var enabledText = ecText.getText("generic.enabled").setStyle(accentStyle);

        var actualMessage = ecText.getText("cmd.fly.feedback", enabledText, playerNameText);

        var expectedString = expectedMessage.getString();
        var actualString = actualMessage.getString();

        assertEquals(expectedString, actualString);

        // this guarantee is gone after some lib upgrades
//        var expectedSiblings = expectedMessage.getSiblings();
//        var actualSiblings = actualMessage.getSiblings();
//        for (int i = 0; i < expectedSiblings.size(); i++) {
//            var inputToken = expectedSiblings.get(i);
//            var actualToken = actualSiblings.get(i);
//
//            assertEquals(inputToken.getContent(), actualToken.getContent());
//            assertEquals(inputToken.getStyle(), actualToken.getStyle());
//        }
    }

    @Test
    @DisplayName("getTextInternal - first token interpolated")
    void getTextInternal_FirstTokenInterpolated_FormatsCorrectly()
    {
        var playerNameText = Component.empty()
            .append(Component.literal("[UnstyledPrefix] "))
            .append(Component.literal("Steve").withStyle(ChatFormatting.AQUA));
        var defaultStyle = TextFormatType.Default.getStyle();
        var expectedMessage = Component.empty()
            .append(playerNameText)
            .append(Component.literal(" is now AFK.").setStyle(defaultStyle));

        var actualMessage = ecText.getText("player.afk.enter", playerNameText);

        var expectedString = expectedMessage.getString();
        var actualString = actualMessage.getString();

        assertEquals(expectedString, actualString);

        // This guarantee is gone after some lib upgrades
//        var expectedSiblings = expectedMessage.getSiblings();
//        var actualSiblings = actualMessage.getSiblings();
//        for (int i = 0; i < expectedSiblings.size(); i++) {
//            var inputToken = expectedSiblings.get(i);
//            var actualToken = actualSiblings.get(i);
//
//            assertEquals(inputToken.getContent(), actualToken.getContent());
//            assertEquals(inputToken.getStyle(), actualToken.getStyle());
//        }
    }
}
