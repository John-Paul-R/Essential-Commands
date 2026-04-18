package com.fibermc.essentialcommands.text;

import com.fibermc.essentialcommands.types.IStyleProvider;
import xyz.nucleoid.server.translations.api.LocalizationTarget;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class PlayerECTextImpl extends ECTextImpl {
    private final IStyleProvider styleProvider;

    public PlayerECTextImpl(LocalizationTarget target, IStyleProvider styleProvider) {
        super(target::getLanguageCode);
        this.styleProvider = styleProvider;
    }

    @Override
    public MutableComponent getTextLiteral(String key, TextFormatType textFormatType) {
        return Component.literal(getString(key)).setStyle(styleProvider.getStyle(textFormatType));
    }

    @Override
    public MutableComponent getText(String key, Component... args) {
        return buildText(key, TextFormatType.Default, this.styleProvider, args);
    }

    @Override
    public MutableComponent getText(String key, TextFormatType textFormatType, Component... args) {
        return buildText(key, textFormatType, this.styleProvider, args);
    }

    @Override
    public MutableComponent literal(String str) {
        return Component.literal(str).setStyle(this.styleProvider.getStyle(TextFormatType.Default));
    }

    @Override
    public MutableComponent accent(String str) {
        return Component.literal(str).setStyle(this.styleProvider.getStyle(TextFormatType.Accent));
    }

    @Override
    public MutableComponent error(String str) {
        return Component.literal(str).setStyle(this.styleProvider.getStyle(TextFormatType.Error));
    }
}
