package com.fibermc.essentialcommands.config;

import net.minecraft.text.Style;

import dev.jpcode.eccore.config.ConfigOption;
import dev.jpcode.eccore.config.ConfigSectionSkeleton;
import dev.jpcode.eccore.config.ConfigUtil;
import dev.jpcode.eccore.config.Option;

import static dev.jpcode.eccore.config.ConfigUtil.parseStyle;

public class TextConfig extends ConfigSectionSkeleton {
    @ConfigOption
    public final Option<Style> FORMATTING_DEFAULT                                       = new Option<>("formatting_default", parseStyle("gold"), ConfigUtil::parseStyle, ConfigUtil::serializeStyle);
    @ConfigOption public final Option<Style> FORMATTING_ACCENT                                        = new Option<>("formatting_accent", parseStyle("light_purple"), ConfigUtil::parseStyle, ConfigUtil::serializeStyle);
    @ConfigOption public final Option<Style> FORMATTING_ERROR                                         = new Option<>("formatting_error", parseStyle("red"), ConfigUtil::parseStyle, ConfigUtil::serializeStyle);

    protected TextConfig(String name) {
        super(name);
    }
}
