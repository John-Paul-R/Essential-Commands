package com.fibermc.essentialcommands.text;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.fibermc.essentialcommands.types.IStyleProvider;
import eu.pb4.placeholders.api.ParserContext;
import eu.pb4.placeholders.api.ServerPlaceholderContext;
import eu.pb4.placeholders.api.parsers.NodeParser;
import eu.pb4.placeholders.api.parsers.TagLikeParser;
import org.jetbrains.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.StringDecomposer;

public class ECTextImpl extends ECText {
    private final ParserContext parserContext;

    public ECTextImpl(
        Map<String, String> stringMap,
        ParserContext parserContext)
    {
        super(stringMap);
        // In normal operation, `server` should always be present. For testing and other contexts,
        // that is not guaranteed. This is admittedly a bit hacky.
        this.parserContext = parserContext;
    }

    public static ECText forServer(Map<String, String> stringMap, MinecraftServer server) {
        return new ECTextImpl(
            stringMap,
            ServerPlaceholderContext.of(server).asParserContext()
        );
    }

    public String getString(String key) {
        return super.stringMap.getOrDefault(key, key);
    }

    // Literals
    public MutableComponent getTextLiteral(String key, TextFormatType textFormatType) {
        return getTextLiteral(key, textFormatType, null);
    }

    public MutableComponent getTextLiteral(
        String key,
        TextFormatType textFormatType,
        @Nullable IStyleProvider styleProvider)
    {
        return Component.literal(getString(key))
            .setStyle(styleProvider == null
                ? textFormatType.getStyle()
                : styleProvider.getStyle(textFormatType));
    }

    // Interpolated
    public MutableComponent getText(String key, Component... args) {
        return getTextInternal(key, TextFormatType.Default, null, args);
    }

    public MutableComponent getText(String key, TextFormatType textFormatType, Component... args) {
        return getTextInternal(key, textFormatType, null, args);
    }

    public MutableComponent getText(String key, TextFormatType textFormatType, IStyleProvider styleProvider, Component... args) {
        return getTextInternal(key, textFormatType, styleProvider, args);
    }

    private NodeParser parserForContext(
        TextFormatType textFormatType,
        @Nullable IStyleProvider styleProvider,
        List<MutableComponent> args)
    {
        return TagLikeParser.of(
            TagLikeParser.PLACEHOLDER_USER,
            TagLikeParser.Provider.placeholderText(placeholderId -> {
                if (placeholderId.startsWith("l:")) {
                    // handling the ${l:lang.key.here} case for interpolating value from elsewhere in language files
                    var idxAndFormattingCode = placeholderId.split(":");
                    if (idxAndFormattingCode.length < 2) {
                        throw new IllegalArgumentException(
                            "Specified lang interpolation prefix ('l'), but no lang key was provided. Expected the form: 'l:lang.key.here'. Received: "
                                + placeholderId);
                    }
                    if (idxAndFormattingCode.length > 3) {
                        throw new IllegalArgumentException("lang string placeholder had an unexpected second ':'. Received: " + placeholderId);
                    }

                    return getTextInternal(idxAndFormattingCode[1], textFormatType, styleProvider);
                }

                if (placeholderId.matches("\\d+")) {
                    // handling the ${1} case for argument interpolation
                    int targetIndex = Integer.parseInt(placeholderId);
                    if (targetIndex > args.size()) {
                        throw new IllegalArgumentException("Invalid 'Argument' placeholder: targeted argument with (0-based) index '" + targetIndex + "' but only " + args.size() + " were present");
                    }
                    return args.get(targetIndex);
                }

                return null;
            })
        );
    }

    private static int hashText(Component text) {
        return Objects.hash(text.getContents(), text.getStyle());
    }

    public MutableComponent getTextInternal(
        String key,
        TextFormatType textFormatType,
        @Nullable IStyleProvider styleProvider,
        Component... args)
    {
        var argsList = Arrays.stream(args).map(Component::copy).toList();
        var argsHashes = argsList.stream()
            .map(ECTextImpl::hashText)
            .collect(Collectors.toCollection(HashSet::new));

        var parser = parserForContext(textFormatType, styleProvider, argsList);
        var parsedText = parser.parseComponent(
            getString(key),
            this.parserContext
        );

        var specifiedStyle = styleProvider == null
            ? textFormatType.getStyle()
            : styleProvider.getStyle(textFormatType);

        return visitText(
            parsedText,
            defaultStylesVisitor(
                // currently using reference equality -- if internals of TextPlaceholderAPI change, this might not be ok
                (node) -> argsHashes.contains(hashText(node))
                    ? DEFAULT_ARGUMENT_STYLE
                    : specifiedStyle
            ),
            // we should stop traversal downward in the tree when we hit one of the args
            (node) -> argsHashes.contains(hashText(node))
        );
    }

    interface TextVisitor {
        MutableComponent accept(Component text);
    }

    private static final Style DEFAULT_ARGUMENT_STYLE = Style.EMPTY.withColor(ChatFormatting.WHITE);

    TextVisitor defaultStylesVisitor(Function<Component, @Nullable Style> defaultStyleProvider) {
        return text -> {
            MutableComponent txt = text.copy();
            var defaultStyleForText = defaultStyleProvider.apply(txt);
            if (defaultStyleForText != null) {
                txt.setStyle(txt.getStyle().applyTo(defaultStyleForText));
            }
            return txt;
        };
    }

    MutableComponent visitText(Component root, TextVisitor textVisitor, Predicate<Component> shouldStopAfter) {
        var txt = textVisitor.accept(root);
        if (shouldStopAfter.test(root)) {
            return txt;
        }
        var siblings = txt.getSiblings();
        for (int i = 0; i < siblings.size(); i++) {
            // DO NOT USE replaceAll here it may lead to exceptions (may be unsupported)
            siblings.set(i, visitText(siblings.get(i), textVisitor, shouldStopAfter));
        }
        return txt;
    }

    public boolean hasTranslation(String key) {
        return super.stringMap.containsKey(key);
    }

    public boolean isRightToLeft() {
        return false;
    }

    public FormattedCharSequence reorder(FormattedText text) {
        return (visitor) ->
            text.visit((style, string) ->
                StringDecomposer.iterateFormatted(string, style, visitor)
                    ? Optional.empty()
                    : FormattedText.STOP_ITERATION, Style.EMPTY).isPresent();
    }

}
