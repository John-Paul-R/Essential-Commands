package com.fibermc.essentialcommands;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Nullable;

import eu.pb4.placeholders.api.*;
import eu.pb4.placeholders.api.node.TextNode;

import net.minecraft.client.font.TextVisitFactory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.*;

import dev.jpcode.eccore.util.TextUtil;

public class ECTextImpl extends ECText {
    private final ParserContext parserContext;

    public ECTextImpl(Map<String, String> stringMap, @Nullable MinecraftServer server) {
        super(stringMap);
        // In normal operation, `server` should always be present. For testing and other contexts,
        // that is not guaranteed. This is admittedly a bit hacky.
        parserContext = server != null
            ? ParserContext.of(PlaceholderContext.KEY, PlaceholderContext.of(server))
            : ParserContext.of();
    }

    public String getString(String key) {
        return super.stringMap.getOrDefault(key, key);
    }

    // Literals
    public MutableText getTextLiteral(String key, TextFormatType textFormatType) {
        return Text.literal(getString(key)).setStyle(textFormatType.getStyle());
    }

    public MutableText getText(String key) {
        return getTextLiteral(key, TextFormatType.Default);
    }

    public MutableText getText(String key, TextFormatType textFormatType) {
        return getTextLiteral(key, textFormatType);
    }

    // Interpolated
    public MutableText getText(String key, Text... args) {
        return getTextInternal(key, TextFormatType.Default, args);
    }

    public MutableText getText(String key, TextFormatType textFormatType, Text... args) {
        return getTextInternal(key, textFormatType, args);
    }

    private Placeholders.PlaceholderGetter placeholderGetterForContext(TextFormatType textFormatType, List<MutableText> args) {
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
                            yield ECTextImpl.this.getTextLiteral(idxAndFormattingCode[1], textFormatType);
                        }

                        default -> args.get(Integer.parseInt(idxAndFormattingCode[0]));
                    };
                    return PlaceholderResult.value(text);
                };
            }
        };
    }

    public MutableText getTextInternal(String key, TextFormatType textFormatType, Text... args) {
        var argsList = Arrays.stream(args).map(Text::copy).toList();
        var placeholderGetter = placeholderGetterForContext(textFormatType, argsList);
        var retVal = Placeholders.parseNodes(
                TextNode.convert(Text.literal(getString(key))),
                Placeholders.PREDEFINED_PLACEHOLDER_PATTERN,
                placeholderGetter)
            .toText(parserContext, true); // Not 100% sure if this flag should be true or false

        var retValSiblings = retVal.getSiblings();

        if (retValSiblings.size() == 0) {
            return retVal.copy();
        }

        var specifiedStyle = textFormatType.getStyle();

        return TextUtil.flattenRoot(retVal)
            .stream()
            .map(text -> argsList.contains(text)
                ? text
                : text.copy().setStyle(specifiedStyle))
            .collect(TextUtil.collect());
    }

    // Other stuff
    public MutableText getText(String key, Object... args) {
        return ECText.literal(String.format(getString(key), args));
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
