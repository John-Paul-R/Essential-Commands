package com.fibermc.essentialcommands.types;

import java.util.function.UnaryOperator;

import com.fibermc.essentialcommands.text.TextFormatType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.text.Style;

public interface IStyleProvider {
    @Nullable Style getFormattingDefault();

    @Nullable Style getFormattingAccent();

    @Nullable Style getFormattingError();

    @NotNull
    default Style getStyle(TextFormatType textFormatType) {
        var styleOverride = switch (textFormatType) {
            case Default -> getFormattingDefault();
            case Accent -> getFormattingAccent();
            case Error -> getFormattingError();
            case Empty -> null;
        };
        return styleOverride != null
            ? styleOverride
            : textFormatType.getStyle();
    }

    default UnaryOperator<Style> nonOverwritingColorUpdater(TextFormatType textFormatType) {
        return (s) -> {
            // The goal here is to overwrite defaults, but not custom styles (e.g. from nickname).
            boolean shouldApply = s.getColor() == null
                || s == TextFormatType.Default.getStyle()
                || s == TextFormatType.Accent.getStyle()
                || s == TextFormatType.Error.getStyle()
                || s == TextFormatType.Empty.getStyle()
                || s == getFormattingDefault()
                || s == getFormattingAccent()
                || s == getFormattingError()
                ;
            return shouldApply ? s.withColor(getStyle(textFormatType).getColor()) : s;
        };
    }
}
