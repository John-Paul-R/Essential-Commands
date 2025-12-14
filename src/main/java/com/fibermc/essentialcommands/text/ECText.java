package com.fibermc.essentialcommands.text;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.playerdata.PlayerProfile;
import com.fibermc.essentialcommands.types.IStyleProvider;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import eu.pb4.placeholders.api.ParserContext;
import eu.pb4.placeholders.api.PlaceholderContext;
import org.jetbrains.annotations.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.GsonHelper;

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
                LOGGER.info(String.format("No EC lang file for the language '%s' found. Defaulting to 'en_us'.", langId));
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
            String string = TOKEN_PATTERN.matcher(GsonHelper.convertToString(value, key)).replaceAll("%$1s");
            entryConsumer.accept(key, string);
        }
    }

    public static ECText getInstance() {
        return instance;
    }

    public abstract String getString(String key);

    public abstract MutableComponent getText(String key, Component... args);

    public abstract MutableComponent getText(String key, TextFormatType textFormatType, Component... args);

    //    public abstract MutableText getText(String key, Object... args);
    public abstract MutableComponent getText(String key, TextFormatType textFormatType, IStyleProvider styleProvider, Component... args);

    public abstract boolean hasTranslation(String key);

    public abstract boolean isRightToLeft();

    public abstract FormattedCharSequence reorder(FormattedText text);

    public MutableComponent literal(String str) {
        return Component.literal(str).setStyle(CONFIG.FORMATTING_DEFAULT);
    }

    public MutableComponent accent(String str) {
        return Component.literal(str).setStyle(CONFIG.FORMATTING_ACCENT);
    }

    public MutableComponent error(String str) {
        return Component.literal(str).setStyle(CONFIG.FORMATTING_ERROR);
    }

    public List<FormattedCharSequence> reorder(List<FormattedText> texts) {
        return texts.stream().map(this::reorder).collect(ImmutableList.toImmutableList());
    }

    public static ECText forPlayer(ServerPlayer player) {
        return new PlayerECTextImpl(
            ECText.getInstance().stringMap,
            ParserContext.of(PlaceholderContext.KEY, PlaceholderContext.of(player)),
            PlayerProfile.access(player)
        );
    }

    public static ECText access(@Nullable ServerPlayer player) {
        return player == null ? ECText.getInstance() : ((ServerPlayerEntityAccess) player).ec$getEcText();
    }
}
