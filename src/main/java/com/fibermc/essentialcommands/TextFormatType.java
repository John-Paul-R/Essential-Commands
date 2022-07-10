package com.fibermc.essentialcommands;

import dev.jpcode.eccore.config.Option;
import net.minecraft.text.Style;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;

public enum TextFormatType {
    Default (CONFIG.FORMATTING_DEFAULT),
    Accent  (CONFIG.FORMATTING_ACCENT),
    Error   (CONFIG.FORMATTING_ERROR);

    private final Option<Style> _style;

    public Style getStyle() {
        return _style.getValue();
    }
    private TextFormatType(Option<Style> style) {
        this._style = style;
    }
}
