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
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.ChatFormatting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

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
            ecText = ECText.getInstance();
        } catch (NoSuchElementException ignored) {
            // We don't care about this. Is a startup error in EssentialCommands.java relating to loading ModMetadata.
        }
    }

    @Test
    @DisplayName("getText - emits translatable with style applied")
    void getText_NoArgs_EmitsTranslatable() {
        var component = ecText.getText("generic.enabled");

        var contents = assertInstanceOf(TranslatableContents.class, component.getContents());
        assertEquals("generic.enabled", contents.getKey());
        assertEquals(0, contents.getArgs().length);
        assertEquals(TextFormatType.Default.getStyle(), component.getStyle());
    }

    @Test
    @DisplayName("getText - two args become translatable args")
    void getText_TwoArgs_ArgsArePassedThrough() {
        var enabledText = ecText.getText("generic.enabled");
        var playerNameText = Component.literal("Steve").withStyle(ChatFormatting.AQUA);

        var actual = ecText.getText("cmd.fly.feedback", enabledText, playerNameText);

        var contents = assertInstanceOf(TranslatableContents.class, actual.getContents());
        assertEquals("cmd.fly.feedback", contents.getKey());
        assertEquals(2, contents.getArgs().length);
        assertEquals(enabledText, contents.getArgs()[0]);
        assertEquals(playerNameText, contents.getArgs()[1]);
    }

    @Test
    @DisplayName("getText - unstyled arg is stamped with white color")
    void getText_UnstyledArg_GetsWhiteColor() {
        var unstyledArg = Component.literal("Steve");

        var actual = ecText.getText("player.afk.enter", unstyledArg);

        var contents = assertInstanceOf(TranslatableContents.class, actual.getContents());
        assertEquals(1, contents.getArgs().length);
        var preppedArg = (Component) contents.getArgs()[0];
        assertEquals(ChatFormatting.WHITE.getColor().intValue(), preppedArg.getStyle().getColor().getValue());
    }

    @Test
    @DisplayName("getText - already-styled arg is preserved as-is")
    void getText_StyledArg_KeepsItsColor() {
        var styledArg = Component.literal("Steve").withStyle(ChatFormatting.AQUA);

        var actual = ecText.getText("player.afk.enter", styledArg);

        var contents = assertInstanceOf(TranslatableContents.class, actual.getContents());
        var preppedArg = (Component) contents.getArgs()[0];
        assertEquals(ChatFormatting.AQUA.getColor().intValue(), preppedArg.getStyle().getColor().getValue());
    }

    @Test
    @DisplayName("getText - error format type applies error style")
    void getText_ErrorFormat_AppliesErrorStyle() {
        var actual = ecText.getText("generic.enabled", TextFormatType.Error);
        assertEquals(TextFormatType.Error.getStyle(), actual.getStyle());
    }
}
