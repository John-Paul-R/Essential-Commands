package com.fibermc.essentialcommands.text;

import java.util.*;
import java.util.stream.Collectors;

import com.fibermc.essentialcommands.types.ECPlaceholderApiCompat;
import com.fibermc.essentialcommands.types.IStyleProvider;
import eu.pb4.placeholders.api.*;
import eu.pb4.placeholders.api.node.TextNode;
import org.jetbrains.annotations.Nullable;

import net.minecraft.server.MinecraftServer;
import net.minecraft.text.*;

import dev.jpcode.eccore.util.TextUtil;

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
            ParserContext.of(PlaceholderContext.KEY, PlaceholderContext.of(server))
        );
    }

    public String getString(String key) {
        return super.stringMap.getOrDefault(key, key);
    }

    // Literals
    public MutableText getTextLiteral(String key, TextFormatType textFormatType) {
        return getTextLiteral(key, textFormatType, null);
    }

    public MutableText getTextLiteral(
        String key,
        TextFormatType textFormatType,
        @Nullable IStyleProvider styleProvider)
    {
        return Text.literal(getString(key))
            .setStyle(styleProvider == null
                ? textFormatType.getStyle()
                : styleProvider.getStyle(textFormatType));
    }

    // Interpolated
    public MutableText getText(String key, Text... args) {
        return getTextInternal(key, TextFormatType.Default, null, args);
    }

    public MutableText getText(String key, TextFormatType textFormatType, Text... args) {
        return getTextInternal(key, textFormatType, null, args);
    }

    public MutableText getText(String key, TextFormatType textFormatType, IStyleProvider styleProvider, Text... args) {
        return getTextInternal(key, textFormatType, styleProvider, args);
    }

    private Placeholders.PlaceholderGetter placeholderGetterForContext(
        TextFormatType textFormatType,
        @Nullable IStyleProvider styleProvider,
        List<MutableText> args)
    {
        return new Placeholders.PlaceholderGetter() {
            @Override
            public boolean isContextOptional() {
                // In some situations (Notably, unit tests), the MinecraftServer from which to init
                // the PlaceholderContext will be unavailable, so we set the context to Optional.
                // (At time of writing, none of EC's PlaceholderGetter tech depends on the server
                // context)
                return true;
            }

            @Override
            public PlaceholderHandler getPlaceholder(String placeholderId) {
                return (ctx, abc) -> {
                    var idxAndFormattingCode = placeholderId.split(":");
                    if (idxAndFormattingCode.length < 1) {
                        throw new IllegalArgumentException("lang string placeholder did not contain an index");
                    }

                    var firstToken = idxAndFormattingCode[0];
                    var text = switch (firstToken) {
                        case "l" -> {
                            if (idxAndFormattingCode.length < 2) {
                                throw new IllegalArgumentException(
                                    "Specified lang interpolation prefix ('l'), but no lang key was provided. Expected the form: 'l:lang.key.here'. Received: "
                                        + placeholderId);
                            }
                            yield getTextInternal(idxAndFormattingCode[1], textFormatType, styleProvider);
                        }

                        default -> args.get(Integer.parseInt(idxAndFormattingCode[0]));
                    };
                    return PlaceholderResult.value(text);
                };
            }
        };
    }

    private static int hashText(Text text) {
        return Objects.hash(text.getContent(), text.getStyle());
    }

    public MutableText getTextInternal(
        String key,
        TextFormatType textFormatType,
        @Nullable IStyleProvider styleProvider,
        Text... args)
    {
        var argsList = Arrays.stream(args).map(Text::copy).toList();
        var argsHashes = argsList.stream()
            .map(ECTextImpl::hashText)
            .collect(Collectors.toCollection(HashSet::new));

        var placeholderGetter = placeholderGetterForContext(textFormatType, styleProvider, argsList);
        var nodes = Placeholders.parseNodes(
            TextNode.convert(Text.literal(getString(key))),
            Placeholders.PREDEFINED_PLACEHOLDER_PATTERN,
            placeholderGetter);
        var retVal = ECPlaceholderApiCompat.toText(nodes, parserContext);

        var retValSiblings = retVal.getSiblings();

        var specifiedStyle = styleProvider == null
            ? textFormatType.getStyle()
            : styleProvider.getStyle(textFormatType);

        if (retValSiblings.size() == 0) {
            return retVal.copy().setStyle(specifiedStyle);
        }

        return retValSiblings
            .stream()
            .map(text -> argsHashes.contains(hashText(text))
                ? text
                : text.copy().setStyle(specifiedStyle))
            .collect(TextUtil.collect());
    }

    public boolean hasTranslation(String key) {
        return super.stringMap.containsKey(key);
    }

    public boolean isRightToLeft() {
        return false;
    }

    public OrderedText reorder(StringVisitable text) {
        return (visitor) ->
            text.visit((style, string) ->
                TextVisitFactory.visitFormatted(string, style, visitor)
                    ? Optional.empty()
                    : StringVisitable.TERMINATE_VISIT, Style.EMPTY).isPresent();
    }

}
