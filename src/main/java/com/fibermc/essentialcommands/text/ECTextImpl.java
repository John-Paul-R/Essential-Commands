package com.fibermc.essentialcommands.text;

import java.util.*;
import java.util.stream.Collectors;

import com.fibermc.essentialcommands.types.IStyleProvider;
import eu.pb4.placeholders.PlaceholderAPI;
import eu.pb4.placeholders.PlaceholderHandler;
import eu.pb4.placeholders.PlaceholderResult;
import org.jetbrains.annotations.Nullable;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import dev.jpcode.eccore.util.TextUtil;

public class ECTextImpl extends ECText {
    private final MinecraftServer serverParserContext;
    private final ServerPlayerEntity playerParserContext;

//    private final MinecraftServer parserContext;

    public ECTextImpl(
        Map<String, String> stringMap,
        MinecraftServer playerParserContext)
    {
        super(stringMap);
        // In normal operation, `server` should always be present. For testing and other contexts,
        // that is not guaranteed. This is admittedly a bit hacky.
        this.serverParserContext = playerParserContext;
        this.playerParserContext = null;
    }

    public ECTextImpl(
        Map<String, String> stringMap,
        ServerPlayerEntity playerParserContext)
    {
        super(stringMap);
        this.serverParserContext = null;
        this.playerParserContext = playerParserContext;
    }

    public static ECText forServer(Map<String, String> stringMap, MinecraftServer server) {
        return new ECTextImpl(stringMap, server);
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
        return TextUtil.literal(getString(key))
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

    // TODO Later, make this work on 1.18.2
//    private Placeholders.PlaceholderGetter placeholderGetterForContext(
//        TextFormatType textFormatType,
//        @Nullable IStyleProvider styleProvider,
//        List<MutableText> args)
//    {
//        return new Placeholders.PlaceholderGetter() {
//            @Override
//            public boolean isContextOptional() {
//                // In some situations (Notably, unit tests), the MinecraftServer from which to init
//                // the PlaceholderContext will be unavailable, so we set the context to Optional.
//                // (At time of writing, none of EC's PlaceholderGetter tech depends on the server
//                // context)
//                return true;
//            }
//
//            @Override
//            public PlaceholderHandler getPlaceholder(String placeholderId) {
//                return (ctx, abc) -> {
//                    var idxAndFormattingCode = placeholderId.split(":");
//                    if (idxAndFormattingCode.length < 1) {
//                        throw new IllegalArgumentException("lang string placeholder did not contain an index");
//                    }
//
//                    var firstToken = idxAndFormattingCode[0];
//                    var text = switch (firstToken) {
//                        case "l" -> {
//                            if (idxAndFormattingCode.length < 2) {
//                                throw new IllegalArgumentException(
//                                    "Specified lang interpolation prefix ('l'), but no lang key was provided. Expected the form: 'l:lang.key.here'. Received: "
//                                        + placeholderId);
//                            }
//                            yield getTextInternal(idxAndFormattingCode[1], textFormatType, styleProvider);
//                        }
//
//                        default -> args.get(Integer.parseInt(idxAndFormattingCode[0]));
//                    };
//                    return PlaceholderResult.value(text);
//                };
//            }
//        };
//    }

    private static int hashText(Text text) {
        return Objects.hash(text.getString(), text.getStyle());
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

        HashMap<Identifier, PlaceholderHandler> argumentsMap = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            argumentsMap.put(
                new Identifier("ec", String.valueOf(i)),
                (a) -> PlaceholderResult.value(args[Integer.parseInt(a.getIdentifier().getPath())]));
        }
        // Really this should be implemented per-extending class, in those
        // extending classes, but I cannot be bothered rn.
        var retVal = playerParserContext != null
            ? PlaceholderAPI.parseTextCustom(
                TextUtil.literal(placeholderAsIdentifier_1_18_Compat(getString(key))),
                playerParserContext,
                argumentsMap,
                PlaceholderAPI.PREDEFINED_PLACEHOLDER_PATTERN)
            : PlaceholderAPI.parseTextCustom(
                TextUtil.literal(placeholderAsIdentifier_1_18_Compat(getString(key))),
                serverParserContext,
                argumentsMap,
                PlaceholderAPI.PREDEFINED_PLACEHOLDER_PATTERN);

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

    private String placeholderAsIdentifier_1_18_Compat(String langTemplateText) {
        return PlaceholderAPI.PREDEFINED_PLACEHOLDER_PATTERN.matcher(langTemplateText).replaceAll("ec:$1");
    }

    public boolean hasTranslation(String key) {
        return super.stringMap.containsKey(key);
    }

    public boolean isRightToLeft() {
        return false;
    }

}
