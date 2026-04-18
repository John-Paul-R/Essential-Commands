package com.fibermc.essentialcommands.text;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fibermc.essentialcommands.types.IStyleProvider;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.server.translations.api.Localization;
import xyz.nucleoid.server.translations.api.LocalizationTarget;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public class ECTextImpl extends ECText {
    /** Marker for inline-key references that survived from the legacy template syntax. */
    private static final Pattern NESTED_REF_PATTERN = Pattern.compile("\\$\\{l:([^}]+)\\}");
    /** Synthetic key prefix used when we have to pre-resolve a template containing nested refs. */
    private static final String SYNTH_KEY_PREFIX = "essentialcommands.__inline__.";
    /** Color stamped onto unstyled args so they don't inherit the template's accent/error color. */
    private static final ChatFormatting DEFAULT_ARGUMENT_COLOR = ChatFormatting.WHITE;

    private final LanguageCodeSupplier targetLanguageCode;

    public ECTextImpl(LanguageCodeSupplier targetLanguageCode) {
        this.targetLanguageCode = targetLanguageCode;
    }

    @FunctionalInterface
    public interface LanguageCodeSupplier {
        @Nullable String get();
    }

    private LocalizationTarget target() {
        String code = this.targetLanguageCode.get();
        return () -> code;
    }

    @Override
    public String getString(String key) {
        String resolved = Localization.raw(key, target());
        return resolved != null ? resolved : key;
    }

    @Override
    public MutableComponent getTextLiteral(String key, TextFormatType textFormatType) {
        return Component.literal(getString(key)).setStyle(styleFor(textFormatType, null));
    }

    @Override
    public MutableComponent getText(String key, Component... args) {
        return buildText(key, TextFormatType.Default, null, args);
    }

    @Override
    public MutableComponent getText(String key, TextFormatType textFormatType, Component... args) {
        return buildText(key, textFormatType, null, args);
    }

    @Override
    public MutableComponent getText(String key, TextFormatType textFormatType, IStyleProvider styleProvider, Component... args) {
        return buildText(key, textFormatType, styleProvider, args);
    }

    protected MutableComponent buildText(
        String key,
        TextFormatType textFormatType,
        @Nullable IStyleProvider styleProvider,
        Component... args
    ) {
        Style outerStyle = styleFor(textFormatType, styleProvider);
        Object[] preppedArgs = prepArgs(args);

        String template = Localization.raw(key, target());
        if (template == null || !NESTED_REF_PATTERN.matcher(template).find()) {
            // Fast path: vanilla TranslatableContents -- Server-Translations resolves per recipient.
            return Component.translatable(key, preppedArgs).withStyle(outerStyle);
        }

        // Slow path: ${l:nestedKey} present. Pre-resolve the template in our target language,
        // then emit a synthetic-key translatable so vanilla's Formatter-based renderer can splice
        // the user args plus a nested Component.translatable per ${l:...} marker.
        return inlineNestedKeys(key, template, preppedArgs).withStyle(outerStyle);
    }

    private static MutableComponent inlineNestedKeys(String key, String template, Object[] preppedArgs) {
        List<Object> allArgs = new ArrayList<>(preppedArgs.length + 2);
        for (Object a : preppedArgs) allArgs.add(a);

        Matcher m = NESTED_REF_PATTERN.matcher(template);
        StringBuilder rewritten = new StringBuilder();
        while (m.find()) {
            String nestedKey = m.group(1);
            allArgs.add(Component.translatable(nestedKey));
            m.appendReplacement(rewritten, Matcher.quoteReplacement("%" + allArgs.size() + "$s"));
        }
        m.appendTail(rewritten);

        return Component.translatableWithFallback(SYNTH_KEY_PREFIX + key, rewritten.toString(), allArgs.toArray());
    }

    /**
     * Copies each arg, stamping {@link #DEFAULT_ARGUMENT_COLOR} onto any whose root style has no color.
     * Preserves the legacy behavior where unstyled args render white instead of inheriting the template's color.
     */
    private static Object[] prepArgs(Component[] args) {
        Object[] result = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            Component arg = args[i];
            if (arg.getStyle().getColor() == null) {
                result[i] = arg.copy().withStyle(arg.getStyle().withColor(DEFAULT_ARGUMENT_COLOR));
            } else {
                result[i] = arg;
            }
        }
        return result;
    }

    protected Style styleFor(TextFormatType textFormatType, @Nullable IStyleProvider styleProvider) {
        return styleProvider == null ? textFormatType.getStyle() : styleProvider.getStyle(textFormatType);
    }
}
