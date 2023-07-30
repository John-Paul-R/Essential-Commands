package com.fibermc.essentialcommands.config;

import java.nio.file.Path;

import org.jetbrains.annotations.NotNull;

import dev.jpcode.eccore.config.Config;
import dev.jpcode.eccore.config.Option;

import static com.fibermc.essentialcommands.EssentialCommands.LOGGER;

@SuppressWarnings("checkstyle:all")
public final class EssentialCommandsConfig extends Config<EssentialCommandsConfig> {
    public final TextConfig TEXT = new TextConfig("text");
    public final FeatureTogglesConfig FEATURE_TOGGLES = new FeatureTogglesConfig("primary toggles");
    public final NicknameConfig NICKNAME = new NicknameConfig("nickname");
    public final TeleportConfig TELEPORT = new TeleportConfig("teleporting (general)");
    public final RandomTeleportConfig RTP = new RandomTeleportConfig("random teleport (rtp)");
    public final AfkConfig AFK = new AfkConfig("afk");
    public final MiscConfig MISC = new MiscConfig("misc");
    public EssentialCommandsConfig(Path savePath, String displayName, String documentationLink) {
        super(savePath, displayName, documentationLink);
        // This value is only sent on server start/player connect and, so, cannot be updated for all
        // players immediately via the config reload command without a fair bit of hackery.
//        NICKNAME_ABOVE_HEAD.changeEvent.register(ign -> {
//            PlayerDataManager.getInstance().queueNicknameUpdatesForAllPlayers();
//        });
    }


    public static <T> T getValueSafe(@NotNull Option<T> option, T defaultValue) {
        try {
            return option.getValue();
        } catch (Exception ex) {
            // Someone was getting an error with eccore/config/Option not being found when Option.getValue() was called
            // from within ServerPlayerEntityMixin. I can't reproduce, but /shrug
            // We're actually catching a ClassNotFoundException due to mixin weirdness, I think...
            LOGGER.error(ex);
        }
        return defaultValue;
    }

}
