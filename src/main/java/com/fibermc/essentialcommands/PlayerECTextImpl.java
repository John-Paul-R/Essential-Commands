package com.fibermc.essentialcommands;

import java.util.Map;

import com.fibermc.essentialcommands.types.IStyleProvider;
import eu.pb4.placeholders.api.ParserContext;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class PlayerECTextImpl extends ECTextImpl {
    private final IStyleProvider styleProvider;

    public PlayerECTextImpl(Map<String, String> stringMap, ParserContext parserContext, IStyleProvider styleProvider) {
        super(stringMap, parserContext);
        this.styleProvider = styleProvider;
    }

    @Override
    public MutableText getTextLiteral(String key, TextFormatType textFormatType) {
        return getTextLiteral(key, textFormatType, this.styleProvider);
    }

    @Override
    public MutableText getText(String key) {
        return getTextLiteral(key, TextFormatType.Default, this.styleProvider);
    }

    @Override
    public MutableText getText(String key, TextFormatType textFormatType) {
        return getTextLiteral(key, textFormatType, this.styleProvider);
    }

    @Override
    public MutableText getText(String key, Text... args) {
        return getTextInternal(key, TextFormatType.Default, this.styleProvider, args);
    }

    @Override
    public MutableText getText(String key, TextFormatType textFormatType, Text... args) {
        return getTextInternal(key, textFormatType, this.styleProvider, args);
    }

    @Override
    public MutableText literal(String str) {
        return Text.literal(str).setStyle(this.styleProvider.getStyle(TextFormatType.Default));
    }

    @Override
    public MutableText accent(String str) {
        return Text.literal(str).setStyle(this.styleProvider.getStyle(TextFormatType.Accent));
    }

    @Override
    public MutableText error(String str) {
        return Text.literal(str).setStyle(this.styleProvider.getStyle(TextFormatType.Error));
    }
}
