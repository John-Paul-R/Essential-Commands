package com.fibermc.essentialcommands.config;

import java.time.Duration;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import dev.jpcode.eccore.config.ConfigOption;
import dev.jpcode.eccore.config.ConfigSectionSkeleton;
import dev.jpcode.eccore.config.ConfigUtil;
import dev.jpcode.eccore.config.Option;
import dev.jpcode.eccore.util.TextUtil;

import static dev.jpcode.eccore.util.TimeUtil.durationToTicks;

public class AfkConfig extends ConfigSectionSkeleton {
    @ConfigOption public final Option<Text> AFK_PREFIX                                                = new Option<>("afk_prefix", Text.literal("[AFK] ").formatted(Formatting.GRAY), TextUtil::parseText, Text.Serializer::toJson);
    @ConfigOption public final Option<Boolean> INVULN_WHILE_AFK                                       = new Option<>("invuln_while_afk", false, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> AUTO_AFK_ENABLED                                       = new Option<>("auto_afk_enabled", true,  Boolean::parseBoolean);
    @ConfigOption public final Option<Integer> AUTO_AFK_TICKS                                         = new Option<>("auto_afk_time", durationToTicks(Duration.ofMinutes(15)), ConfigUtil::parseDurationToTicks, ConfigUtil::serializeTicksAsDuration);

    protected AfkConfig(String name) {
        super(name);
    }
}
