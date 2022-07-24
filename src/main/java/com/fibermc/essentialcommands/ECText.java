package com.fibermc.essentialcommands;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import dev.jpcode.eccore.util.TextUtil;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
import net.minecraft.client.font.TextVisitFactory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.*;
import net.minecraft.util.JsonHelper;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

import static com.fibermc.essentialcommands.EssentialCommands.*;

public abstract class ECText {

    private static final Gson GSON = new Gson();
    private static final Pattern TOKEN_PATTERN = Pattern.compile("%(\\d+\\$)?[\\d.]*[df]");
    public static final String DEFAULT_LANGUAGE_SPEC = "en_us";

    private static volatile ECText instance = create(CONFIG.LANGUAGE);
    private static MinecraftServer _server;

    private ECText() {}

    static {
        BACKING_CONFIG.LANGUAGE.changeEvent.register((langId) -> instance = create(langId));
    }

    public static void init(MinecraftServer server) {
        _server = server;
    }

    public static MutableText literal(String str) {
        return Text.literal(str).setStyle(CONFIG.FORMATTING_DEFAULT);
    }

    public static MutableText accent(String str) {
        return Text.literal(str).setStyle(CONFIG.FORMATTING_ACCENT);
    }

    public static MutableText error(String str) {
        return Text.literal(str).setStyle(CONFIG.FORMATTING_ERROR);
    }

    private static ECText create(String langId) {
        ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
        final String resourceFString = "/assets/essential_commands/lang/%s.json";
        final String resourceLocation = String.format(resourceFString, langId);
        try {
            InputStream inputStream = ECText.class.getResourceAsStream(resourceLocation);
            if (inputStream == null) {
                LOGGER.info(String.format("No EC lang file for the language '%s' found. Defulting to 'en_us'.", langId));
                inputStream = ECText.class.getResourceAsStream(String.format(resourceFString, DEFAULT_LANGUAGE_SPEC));
            }

            try {
                load(inputStream, builder::put);
            } catch (Throwable loadEx) {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Throwable closeEx) {
                        loadEx.addSuppressed(closeEx);
                    }
                }

                throw loadEx;
            }

            if (inputStream != null) {
                inputStream.close();
            }
        } catch (JsonParseException | IOException ex) {
            LOGGER.error("Couldn't read strings from {}", resourceLocation, ex);
        }

        final Map<String, String> map = builder.build();
        return new ECText() {
            public String get(String key) {
                return map.getOrDefault(key, key);
            }

            // Literals
            public MutableText getTextLiteral(String key, TextFormatType textFormatType) {
                return Text.literal(get(key)).setStyle(textFormatType.getStyle());
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

            public MutableText getText(String key, TextFormatType textFormatType,  Text... args) {
                return getTextInternal(key, textFormatType, args);
            }

            private Placeholders.PlaceholderGetter placeholderGetterForContext(TextFormatType textFormatType, List<MutableText> args) {
                return (placeholderId) ->
                    (ctx, abc) -> {
                        var idxAndFormattingCode = placeholderId.split(":");
                        if (idxAndFormattingCode.length < 1) {
                            throw new IllegalArgumentException("lang string placeholder did not contain an index");
                        }

                        var firstToken = idxAndFormattingCode[0];
                        var text = switch (firstToken) {
                            case "l" -> {
                                if (idxAndFormattingCode.length < 2) {
                                    throw new IllegalArgumentException("Specified lang interpolation prefix ('l'), but no lang key was provided. Expected the form: 'l:lang.key.here'. Received: " + placeholderId);
                                }
                                yield getTextLiteral(idxAndFormattingCode[1], textFormatType);
                            }

                            default -> args.get(Integer.parseInt(idxAndFormattingCode[0]));
                        };
                        return PlaceholderResult.value(text);
                    };
            }

            public MutableText getTextInternal(String key, TextFormatType textFormatType,  Text... args) {
                var argsList = Arrays.stream(args).map(Text::copy).toList();
                var placeholderGetter = placeholderGetterForContext(textFormatType, argsList);
                var retVal = Placeholders.parseText(
                    Text.literal(get(key)),
                    PlaceholderContext.of(_server),
                    Placeholders.PREDEFINED_PLACEHOLDER_PATTERN,
                    placeholderGetter);

                var retValSiblings = retVal.getSiblings();

                var specifiedStyle = textFormatType.getStyle();

                if (retValSiblings.size() == 0) {
                    return retVal.copy();
                }

                return TextUtil.flattenRoot(retVal)
                    .stream()
                    .map(text -> argsList.contains(text)
                        ? text
                        : text.copy().setStyle(specifiedStyle))
                    .collect(TextUtil.collect());
            }

            // Other stuff
            public MutableText getText(String key, Object... args) {
                return ECText.literal(String.format(get(key), args));
            }

            public boolean hasTranslation(String key) {
                return map.containsKey(key);
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
        };
    }

    public static void load(InputStream inputStream, BiConsumer<String, String> entryConsumer) {
        JsonObject jsonObject = GSON.fromJson(
            new InputStreamReader(inputStream, StandardCharsets.UTF_8),
            JsonObject.class);

        for (var stringJsonElementEntry : jsonObject.entrySet()) {
            var key = stringJsonElementEntry.getKey();
            var value = stringJsonElementEntry.getValue();
            String string = TOKEN_PATTERN.matcher(JsonHelper.asString(value, key)).replaceAll("%$1s");
            entryConsumer.accept(key, string);
        }
    }

    public static ECText getInstance() {
        return instance;
    }

//    public static String get(String key) {
//        Language
//    }
    public abstract String get(String key);

    public abstract MutableText getText(String key, Text... args);

    public abstract MutableText getText(String key, TextFormatType textFormatType,  Text... args);

    public abstract MutableText getText(String key, Object... args);

    public abstract MutableText getText(String key);

    public abstract boolean hasTranslation(String key);

    public abstract boolean isRightToLeft();

    public abstract OrderedText reorder(StringVisitable text);

    public List<OrderedText> reorder(List<StringVisitable> texts) {
        return texts.stream().map(this::reorder).collect(ImmutableList.toImmutableList());
    }

}
