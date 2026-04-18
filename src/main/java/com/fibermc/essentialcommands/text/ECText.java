package com.fibermc.essentialcommands.text;

import java.util.List;
import java.util.Optional;

import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.playerdata.PlayerProfile;
import com.fibermc.essentialcommands.types.IStyleProvider;
import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.Nullable;
import xyz.nucleoid.server.translations.api.LocalizationTarget;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.StringDecomposer;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;

public abstract class ECText {
    public static final String DEFAULT_LANGUAGE_SPEC = "en_us";

    private static final ECText SYSTEM_INSTANCE = new ECTextImpl(ECText::systemLanguageCode);

    private static @Nullable String systemLanguageCode() {
        String configured = CONFIG.LANGUAGE;
        return configured == null || configured.isBlank() ? DEFAULT_LANGUAGE_SPEC : configured;
    }

    public static void init(MinecraftServer server) {
        // Server-Translations owns lang loading; nothing to do here. Retained for callers/tests.
    }

    public static ECText getInstance() {
        return SYSTEM_INSTANCE;
    }

    public abstract String getString(String key);

    public abstract MutableComponent getText(String key, Component... args);

    public abstract MutableComponent getText(String key, TextFormatType textFormatType, Component... args);

    public abstract MutableComponent getText(String key, TextFormatType textFormatType, IStyleProvider styleProvider, Component... args);

    public abstract MutableComponent getTextLiteral(String key, TextFormatType textFormatType);

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
        return new PlayerECTextImpl(LocalizationTarget.of(player), PlayerProfile.access(player));
    }

    public static ECText access(@Nullable ServerPlayer player) {
        return player == null ? getInstance() : ((ServerPlayerEntityAccess) player).ec$getEcText();
    }

    static FormattedCharSequence rawReorder(FormattedText text) {
        return visitor -> text.visit(
            (style, string) -> StringDecomposer.iterateFormatted(string, style, visitor)
                ? Optional.empty()
                : FormattedText.STOP_ITERATION,
            Style.EMPTY
        ).isPresent();
    }
}
