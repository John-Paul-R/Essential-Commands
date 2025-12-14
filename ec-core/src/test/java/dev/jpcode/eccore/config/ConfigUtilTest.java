package dev.jpcode.eccore.config;

import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.network.chat.Style;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ConfigUtilTest {
    @BeforeEach
    void setUp() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void testStyleParser() {
        Style expected = Style.EMPTY.withColor(ChatFormatting.RED);
        Style thing = ConfigUtil.parseStyle("{\"color\":\"red\"}");

        assertEquals(thing, expected);
    }
}
