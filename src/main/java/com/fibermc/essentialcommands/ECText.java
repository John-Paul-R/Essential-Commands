package com.fibermc.essentialcommands;

import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.types.IStyleProvider;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import eu.pb4.placeholders.api.ParserContext;
import eu.pb4.placeholders.api.PlaceholderContext;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.JsonHelper;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

import static com.fibermc.essentialcommands.EssentialCommands.*;

public abstract class ECText {
    protected final Map<String, String> stringMap;

    protected ECText(Map<String, String> stringMap) {
        this.stringMap = stringMap;
    }

    private static final Gson GSON = new Gson();
    private static final Pattern TOKEN_PATTERN = Pattern.compile("%(\\d+\\$)?[\\d.]*[df]");
    public static final String DEFAULT_LANGUAGE_SPEC = "en_us";

    private static volatile ECText instance = create(CONFIG.LANGUAGE);
    private static MinecraftServer server;

    static {
        BACKING_CONFIG.LANGUAGE.changeEvent.register((langId) -> instance = create(langId));
    }

    public static void init(MinecraftServer server) {
        ECText.server = server;
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
        return instance = server == null
            ? new ECTextImpl(map, ParserContext.of())
            : ECTextImpl.forServer(map, server);
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

    public abstract String getString(String key);

    public abstract MutableText getText(String key, Text... args);

    public abstract MutableText getText(String key, TextFormatType textFormatType, Text... args);

    //    public abstract MutableText getText(String key, Object... args);
    public abstract MutableText getText(String key, TextFormatType textFormatType, IStyleProvider styleProvider, Text... args);

    public abstract MutableText getText(String key);

    public abstract boolean hasTranslation(String key);

    public abstract boolean isRightToLeft();

    public abstract OrderedText reorder(StringVisitable text);

    public MutableText literal(String str) {
        return Text.literal(str).setStyle(CONFIG.FORMATTING_DEFAULT);
    }

    public MutableText accent(String str) {
        return Text.literal(str).setStyle(CONFIG.FORMATTING_ACCENT);
    }

    public MutableText error(String str) {
        return Text.literal(str).setStyle(CONFIG.FORMATTING_ERROR);
    }

    public List<OrderedText> reorder(List<StringVisitable> texts) {
        return texts.stream().map(this::reorder).collect(ImmutableList.toImmutableList());
    }

    public static ECText forPlayer(ServerPlayerEntity player) {
        return new PlayerECTextImpl(
            ECText.getInstance().stringMap,
            ParserContext.of(PlaceholderContext.KEY, PlaceholderContext.of(player)),
            PlayerProfile.access(player)
        );
    }

    public static ECText access(@NotNull ServerPlayerEntity player) {
        return ((ServerPlayerEntityAccess) player).ec$getEcText();
    }
}
