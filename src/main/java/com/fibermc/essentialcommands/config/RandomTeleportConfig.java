package com.fibermc.essentialcommands.config;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.fibermc.essentialcommands.EssentialCommands;
import com.fibermc.essentialcommands.ManagerLocator;
import org.jetbrains.annotations.NotNull;

import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import dev.jpcode.eccore.config.ConfigOption;
import dev.jpcode.eccore.config.ConfigSectionSkeleton;
import dev.jpcode.eccore.config.ConfigUtil;
import dev.jpcode.eccore.config.Option;

import static dev.jpcode.eccore.config.ConfigUtil.arrayParser;
import static dev.jpcode.eccore.config.ConfigUtil.parseIntOrDefault;

public class RandomTeleportConfig extends ConfigSectionSkeleton {
    @ConfigOption
    public final Option<Integer> RTP_RADIUS                                             = new Option<>("rtp_radius", 1000, ConfigUtil::parseInt);
    @ConfigOption public final Option<Integer> RTP_MIN_RADIUS                                         = new Option<>("rtp_min_radius", RTP_RADIUS.getValue(), (String s) -> parseIntOrDefault(s, RTP_RADIUS.getValue()));
    @ConfigOption public final Option<Integer> RTP_COOLDOWN                                           = new Option<>("rtp_cooldown", 30, ConfigUtil::parseInt);
    @ConfigOption public final Option<Integer> RTP_MAX_ATTEMPTS                                       = new Option<>("rtp_max_attempts", 15, ConfigUtil::parseInt);
    @ConfigOption public final Option<List<String>> RTP_ENABLED_WORLDS                                = new Option<>("rtp_enabled_worlds", List.of(World.OVERWORLD.getValue().getPath()), arrayParser(Object::toString));

    public RandomTeleportConfig(String name) {
        super(name);
        RTP_ENABLED_WORLDS.changeEvent.register(configuredWorldIdStrings -> {
            ManagerLocator.getInstance().runAndQueue("RTP_ENABLED_WORLDS", server -> {
                var worldIds = server.getWorldRegistryKeys().stream()
                    .map(RegistryKey::getValue)
                    .collect(Collectors.toSet());

                EssentialCommands.LOGGER.info("Possible world ids: {}", String.join(",", worldIds.stream().map(Identifier::toString).toList()));

                var configuredWorldIds = configuredWorldIdStrings.stream()
                    .map(Identifier::new)
                    .toList();
                EssentialCommands.LOGGER.info("Configured `rtp_enabled_worlds` world ids: {}", String.join(",", configuredWorldIds.stream().map(Identifier::toString).toList()));

                var validConfiguredWorldIds = configuredWorldIdStrings.stream()
                    .map(Identifier::new)
                    .filter(worldIds::contains)
                    .collect(Collectors.toSet());

                var invalidConfiguredWorldIds = configuredWorldIdStrings.stream()
                    .map(Identifier::new)
                    .filter(v -> !worldIds.contains(v))
                    .toList();

                if (invalidConfiguredWorldIds.size() > 0) {
                    EssentialCommands.LOGGER.warn("{} configured `rtp_enabled_worlds` world ids were invalid: {}", invalidConfiguredWorldIds.size(), String.join(",", invalidConfiguredWorldIds.stream().map(Identifier::toString).toList()));
                } else {
                    EssentialCommands.LOGGER.info("All configured `rtp_enabled_worlds` world ids are valid.");
                }

                this.validRtpWorldIds.clear();
                server.getWorldRegistryKeys().stream()
                    .filter(k -> validConfiguredWorldIds.contains(k.getValue()))
                    .forEach(this.validRtpWorldIds::add);
                EssentialCommands.refreshConfigSnapshot();
            });
        });
    }

    private final HashSet<RegistryKey<World>> validRtpWorldIds = new HashSet<>();
    @NotNull
    public Set<RegistryKey<World>> getValidRtpWorldKeys() {
        return this.validRtpWorldIds;
    }

}
