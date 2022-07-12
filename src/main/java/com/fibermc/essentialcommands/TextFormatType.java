package com.fibermc.essentialcommands;

import dev.jpcode.eccore.config.Option;
import net.minecraft.text.Style;

import java.util.function.UnaryOperator;

import static com.fibermc.essentialcommands.EssentialCommands.BACKING_CONFIG;

public enum TextFormatType {
    Default (BACKING_CONFIG.FORMATTING_DEFAULT),
    Accent  (BACKING_CONFIG.FORMATTING_ACCENT),
    Error   (BACKING_CONFIG.FORMATTING_ERROR);

    private final Option<Style> _style;

    public Style getStyle() {
        return _style.getValue();
    }

    public UnaryOperator<Style> nonOverwritingStyleUpdater() {
        return (s) -> {
            var style = this.getStyle();
            // The goal here is to overwrite defaults, but not custom styles (e.g. from nickname).
            boolean shouldApply = s.getColor() == null
                || s == Default.getStyle()
                || s == Accent.getStyle()
                || s == Error.getStyle();
            return shouldApply ? s.withColor(style.getColor()) : s;
        };
    }

    TextFormatType(Option<Style> style) {
        this._style = style;
    }
}
