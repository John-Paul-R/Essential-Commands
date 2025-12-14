package com.fibermc.essentialcommands.text;

import java.util.Map;

import com.fibermc.essentialcommands.types.IStyleProvider;
import eu.pb4.placeholders.api.ParserContext;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class PlayerECTextImpl extends ECTextImpl {
    private final IStyleProvider styleProvider;

    public PlayerECTextImpl(Map<String, String> stringMap, ParserContext parserContext, IStyleProvider styleProvider) {
        super(stringMap, parserContext);
        this.styleProvider = styleProvider;
    }

    @Override
    public MutableComponent getTextLiteral(String key, TextFormatType textFormatType) {
        return getTextLiteral(key, textFormatType, this.styleProvider);
    }

    @Override
    public MutableComponent getText(String key, Component... args) {
        return getTextInternal(key, TextFormatType.Default, this.styleProvider, args);
    }

    @Override
    public MutableComponent getText(String key, TextFormatType textFormatType, Component... args) {
        return getTextInternal(key, textFormatType, this.styleProvider, args);
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
