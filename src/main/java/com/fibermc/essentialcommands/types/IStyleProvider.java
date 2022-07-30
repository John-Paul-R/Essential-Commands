package com.fibermc.essentialcommands.types;

import com.fibermc.essentialcommands.TextFormatType;
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
}
