package com.fibermc.essentialcommands.config;

import java.nio.file.Path;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.fibermc.essentialcommands.ECPerms;
import com.fibermc.essentialcommands.EssentialCommands;
import com.fibermc.essentialcommands.ManagerLocator;
import com.fibermc.essentialcommands.playerdata.PlayerDataManager;
import com.fibermc.essentialcommands.types.RespawnCondition;
import org.jetbrains.annotations.NotNull;

import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import dev.jpcode.eccore.config.Config;
import dev.jpcode.eccore.config.ConfigOption;
import dev.jpcode.eccore.config.ConfigUtil;
import dev.jpcode.eccore.config.Option;
import dev.jpcode.eccore.config.expression.Expression;
import dev.jpcode.eccore.config.expression.PatternMatchingExpressionReader;
import dev.jpcode.eccore.util.TextUtil;

import static com.fibermc.essentialcommands.EssentialCommands.LOGGER;
import static dev.jpcode.eccore.config.ConfigUtil.*;
import static dev.jpcode.eccore.util.TextUtil.parseText;
import static dev.jpcode.eccore.util.TimeUtil.durationToTicks;

@SuppressWarnings("checkstyle:all")
public final class EssentialCommandsConfig extends Config<EssentialCommandsConfig> {

    @ConfigOption public final Option<Style> FORMATTING_DEFAULT =     new Option<>("formatting_default", parseStyle("gold"), ConfigUtil::parseStyle, ConfigUtil::serializeStyle);
    @ConfigOption public final Option<Style> FORMATTING_ACCENT =      new Option<>("formatting_accent", parseStyle("light_purple"), ConfigUtil::parseStyle, ConfigUtil::serializeStyle);
    @ConfigOption public final Option<Style> FORMATTING_ERROR =       new Option<>("formatting_error", parseStyle("red"), ConfigUtil::parseStyle, ConfigUtil::serializeStyle);
    @ConfigOption public final Option<Text>  NICKNAME_PREFIX =        new Option<>("nickname_prefix", parseText("{\"text\":\"~\",\"color\":\"red\"}"), TextUtil::parseText, Text.Serialization::toJsonString);
    @ConfigOption public final Option<Boolean> ENABLE_BACK =            new Option<>("enable_back", true, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> ENABLE_HOME =            new Option<>("enable_home", true, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> ENABLE_SPAWN =           new Option<>("enable_spawn", true, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> ENABLE_TPA =             new Option<>("enable_tpa", true, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> ENABLE_WARP =            new Option<>("enable_warp", true, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> ENABLE_NICK =            new Option<>("enable_nick", true, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> ENABLE_RTP =             new Option<>("enable_rtp", true, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> ENABLE_FLY =             new Option<>("enable_fly", true, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> ENABLE_INVULN =          new Option<>("enable_invuln", true, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> ENABLE_WORKBENCH =       new Option<>("enable_workbench", true, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> ENABLE_ANVIL =           new Option<>("enable_anvil", false, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> ENABLE_ENDERCHEST =      new Option<>("enable_enderchest", true, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> ENABLE_WASTEBIN =        new Option<>("enable_wastebin", true, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> ENABLE_ESSENTIALSX_CONVERT = new Option<>("enable_experimental_essentialsx_converter", false, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> ENABLE_TOP =             new Option<>("enable_top", true, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> ENABLE_GAMETIME =        new Option<>("enable_gametime", true, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> ENABLE_MOTD =            new Option<>("enable_motd", false, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> ENABLE_AFK =             new Option<>("enable_afk", true, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> ENABLE_DAY =             new Option<>("enable_day", true, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> ENABLE_RULES =           new Option<>("enable_rules", true, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> ENABLE_BED =             new Option<>("enable_bed", false, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> ENABLE_FEED =            new Option<>("enable_feed", true, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> ENABLE_HEAL =            new Option<>("enable_heal", true, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> ENABLE_EXTINGUISH =      new Option<>("enable_extinguish", true, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> ENABLE_SUICIDE =         new Option<>("enable_suicide", true, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> ENABLE_NIGHT =           new Option<>("enable_night", true, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> ENABLE_REPAIR =          new Option<>("enable_repair", true, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> ENABLE_NEAR =            new Option<>("enable_near", true, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> ENABLE_DELETE_ALL_PLAYER_DATA = new Option<>("enable_delete_all_player_data", true, Boolean::parseBoolean);
    @ConfigOption public final Option<List<Integer>> HOME_LIMIT =       new Option<>("home_limit", List.of(1, 2, 5), arrayParser(ConfigUtil::parseInt));
    @ConfigOption public final Option<Double>  TELEPORT_COOLDOWN =      new Option<>("teleport_cooldown", 1.0, ConfigUtil::parseDouble);
    @ConfigOption public final Option<Double>  TELEPORT_DELAY =         new Option<>("teleport_delay", 0.0, ConfigUtil::parseDouble);
    @ConfigOption public final Option<Boolean> ALLOW_BACK_ON_DEATH =    new Option<>("allow_back_on_death", false, Boolean::parseBoolean);
    @ConfigOption public final Option<Integer> TELEPORT_REQUEST_DURATION = new Option<>("teleport_request_duration", 60, ConfigUtil::parseInt);
    @ConfigOption public final Option<Boolean> USE_PERMISSIONS_API =    new Option<>("use_permissions_api", false, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> CHECK_FOR_UPDATES =      new Option<>("check_for_updates", true, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> TELEPORT_INTERRUPT_ON_DAMAGED = new Option<>("teleport_interrupt_on_damaged", true, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> TELEPORT_INTERRUPT_ON_MOVE = new Option<>("teleport_interrupt_on_move", false, Boolean::parseBoolean);
    @ConfigOption public final Option<Double>  TELEPORT_INTERRUPT_ON_MOVE_AMOUNT = new Option<>("teleport_interrupt_on_move_max_blocks", 3D, ConfigUtil::parseDouble);
    @ConfigOption public final Option<Boolean> ALLOW_TELEPORT_BETWEEN_DIMENSIONS = new Option<>("allow_teleport_between_dimensions", true, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> OPS_BYPASS_TELEPORT_RULES =  new Option<>("ops_bypass_teleport_rules", true, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> NICKNAMES_IN_PLAYER_LIST =   new Option<>("nicknames_in_player_list", true, Boolean::parseBoolean);
    @ConfigOption public final Option<Integer> NICKNAME_MAX_LENGTH =    new Option<>("nickname_max_length", 32, ConfigUtil::parseInt);
    @ConfigOption public final Option<Boolean> NICKNAME_ABOVE_HEAD =    new Option<>("nickname_above_head", false, Boolean::parseBoolean);
    @ConfigOption public final Option<Integer> RTP_RADIUS =             new Option<>("rtp_radius", 1000, ConfigUtil::parseInt);
    @ConfigOption public final Option<Integer> RTP_MIN_RADIUS =         new Option<>("rtp_min_radius", RTP_RADIUS.getValue(), (String s) -> parseIntOrDefault(s, RTP_RADIUS.getValue()));
    @ConfigOption public final Option<Integer> RTP_COOLDOWN =           new Option<>("rtp_cooldown", 30, ConfigUtil::parseInt);
    @ConfigOption public final Option<Integer> RTP_MAX_ATTEMPTS =       new Option<>("rtp_max_attempts", 15, ConfigUtil::parseInt);
    @ConfigOption public final Option<List<String>> RTP_ENABLED_WORLDS = new Option<>("rtp_enabled_worlds", List.of(World.OVERWORLD.getValue().getPath()), arrayParser(Object::toString));
    @ConfigOption public final Option<Boolean> BROADCAST_TO_OPS =       new Option<>("broadcast_to_ops", false, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> NICK_REVEAL_ON_HOVER =   new Option<>("nick_reveal_on_hover", true, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> GRANT_LOWEST_NUMERIC_BY_DEFAULT = new Option<>("grant_lowest_numeric_by_default", true, Boolean::parseBoolean);
    @ConfigOption public final Option<String> LANGUAGE = new Option<>("language", "en_us", String::toString);
    @ConfigOption public final Option<String> MOTD = new Option<>("motd", "<yellow>Welcome to our server <blue>%player:displayname%</blue>!\nPlease read the rules.</yellow>", String::toString);
    @ConfigOption public final Option<Text> AFK_PREFIX = new Option<>("afk_prefix", Text.literal("[AFK] ").formatted(Formatting.GRAY), TextUtil::parseText, Text.Serialization::toJsonString);
    @ConfigOption public final Option<Boolean> INVULN_WHILE_AFK = new Option<>("invuln_while_afk", false, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> AUTO_AFK_ENABLED = new Option<>("auto_afk_enabled", true,  Boolean::parseBoolean);
    @ConfigOption public final Option<Integer> AUTO_AFK_TICKS = new Option<>("auto_afk_time", durationToTicks(Duration.ofMinutes(15)), ConfigUtil::parseDurationToTicks, ConfigUtil::serializeTicksAsDuration);
    @ConfigOption public final Option<Boolean> REGISTER_TOP_LEVEL_COMMANDS = new Option<>("register_top_level_commands", true, Boolean::parseBoolean);
    @ConfigOption public final Option<List<String>> EXCLUDED_TOP_LEVEL_COMMANDS = new Option<>("excluded_top_level_commands", List.of(), ConfigUtil.arrayParser(Object::toString));
    @ConfigOption public final Option<Expression<RespawnCondition>> RESPAWN_AT_EC_SPAWN = new Option<>("respawn_at_ec_spawn", Expression.of(RespawnCondition.Never), (str) -> str.isBlank() ? Expression.of(RespawnCondition.Never) : PatternMatchingExpressionReader.parse(str, RespawnCondition::valueOf), Expression::serialize);
    // TODO @1.0.0: Enable PERSIST_BACK_LOCATION by default
    @ConfigOption public final Option<Boolean> PERSIST_BACK_LOCATION = new Option<>("persist_back_location", false, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> RECHECK_PLAYER_ABILITY_PERMISSIONS_ON_DIMENSION_CHANGE = new Option<>("recheck_player_ability_permissions_on_dimension_change", false, Boolean::parseBoolean);
    @ConfigOption public final Option<Integer> FLY_MAX_SPEED = new Option<>("fly_max_speed", 5, ConfigUtil::parseInt);
    @ConfigOption public final Option<Integer> NEAR_COMMAND_DEFAULT_RADIUS = new Option<>("near_command_default_radius", 200, ConfigUtil::parseInt);
    @ConfigOption public final Option<Integer> NEAR_COMMAND_MAX_RADIUS = new Option<>("near_command_max_radius", 200, ConfigUtil::parseInt);

    public EssentialCommandsConfig(Path savePath, String displayName, String documentationLink) {
        super(savePath, displayName, documentationLink);
        HOME_LIMIT.changeEvent.register(newValue ->
                ECPerms.Registry.Group.home_limit_group = ECPerms.makeNumericPermissionGroup("essentialcommands.home.limit", newValue)
        );
        // This value is only sent on server start/player connect and, so, cannot be updated for all
        // players immediately via the config reload command without a fair bit of hackery.
//        NICKNAME_ABOVE_HEAD.changeEvent.register(ign -> {
//            PlayerDataManager.getInstance().queueNicknameUpdatesForAllPlayers();
//        });
        NICKNAMES_IN_PLAYER_LIST.changeEvent.register(ign -> {
            PlayerDataManager.getInstance().queueNicknameUpdatesForAllPlayers();
        });


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
