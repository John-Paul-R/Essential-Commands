package com.fibermc.essentialcommands;

import java.util.function.UnaryOperator;

import net.minecraft.text.Style;

import dev.jpcode.eccore.config.Option;

import static com.fibermc.essentialcommands.EssentialCommands.BACKING_CONFIG;

@SuppressWarnings({"checkstyle:MethodParamPad", "checkstyle:singlespaceseparator"})
public enum TextFormatType {
    Default (BACKING_CONFIG.FORMATTING_DEFAULT),
    Accent  (BACKING_CONFIG.FORMATTING_ACCENT),
    Error   (BACKING_CONFIG.FORMATTING_ERROR),
    Empty   (new Option<>("---", Style.EMPTY, value -> Style.EMPTY));

    private final Option<Style> style;

    public Style getStyle() {
        return style.getValue();
    }

    public UnaryOperator<Style> nonOverwritingStyleUpdater() {
        return (s) -> {
            var style = this.getStyle();
            // The goal here is to overwrite defaults, but not custom styles (e.g. from nickname).
            boolean shouldApply = s.getColor() == null
                || s == Default.getStyle()
                || s == Accent.getStyle()
                || s == Error.getStyle()
                || s == Empty.getStyle();
            return shouldApply ? s.withColor(style.getColor()) : s;
        };
    }

    TextFormatType(Option<Style> style) {
        this.style = style;
    }
}
