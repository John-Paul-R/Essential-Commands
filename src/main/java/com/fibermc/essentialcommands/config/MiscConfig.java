package com.fibermc.essentialcommands.config;

import java.util.List;

import com.fibermc.essentialcommands.ECPerms;
import com.fibermc.essentialcommands.types.RespawnCondition;

import dev.jpcode.eccore.config.ConfigOption;
import dev.jpcode.eccore.config.ConfigSectionSkeleton;
import dev.jpcode.eccore.config.ConfigUtil;
import dev.jpcode.eccore.config.Option;
import dev.jpcode.eccore.config.expression.Expression;
import dev.jpcode.eccore.config.expression.PatternMatchingExpressionReader;

import static dev.jpcode.eccore.config.ConfigUtil.arrayParser;

public class MiscConfig extends ConfigSectionSkeleton {
    @ConfigOption
    public final Option<List<Integer>> HOME_LIMIT                                       = new Option<>("home_limit", List.of(1, 2, 5), arrayParser(ConfigUtil::parseInt));
    @ConfigOption public final Option<Boolean> ALLOW_BACK_ON_DEATH                      = new Option<>("allow_back_on_death", false, Boolean::parseBoolean);
    @ConfigOption public final Option<Integer> TELEPORT_REQUEST_DURATION                = new Option<>("teleport_request_duration", 60, ConfigUtil::parseInt);
    @ConfigOption public final Option<Boolean> USE_PERMISSIONS_API                      = new Option<>("use_permissions_api", false, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> CHECK_FOR_UPDATES                        = new Option<>("check_for_updates", true, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> BROADCAST_TO_OPS                         = new Option<>("broadcast_to_ops", false, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> GRANT_LOWEST_NUMERIC_BY_DEFAULT          = new Option<>("grant_lowest_numeric_by_default", true, Boolean::parseBoolean);
    @ConfigOption public final Option<String> LANGUAGE                                  = new Option<>("language", "en_us", String::toString);
    @ConfigOption public final Option<String> MOTD                                      = new Option<>("motd", "<yellow>Welcome to our server <blue>%player:displayname%</blue>!\nPlease read the rules.</yellow>", String::toString);
    @ConfigOption public final Option<Boolean> REGISTER_TOP_LEVEL_COMMANDS              = new Option<>("register_top_level_commands", true, Boolean::parseBoolean);
    @ConfigOption public final Option<List<String>> EXCLUDED_TOP_LEVEL_COMMANDS         = new Option<>("excluded_top_level_commands", List.of(), ConfigUtil.arrayParser(Object::toString));
    @ConfigOption public final Option<Expression<RespawnCondition>> RESPAWN_AT_EC_SPAWN = new Option<>("respawn_at_ec_spawn", Expression.of(RespawnCondition.Never), (str) -> str.isBlank() ? Expression.of(RespawnCondition.Never) : PatternMatchingExpressionReader.parse(str, RespawnCondition::valueOf), Expression::serialize);
    // TODO @1.0.0: Enable PERSIST_BACK_LOCATION by default
    @ConfigOption public final Option<Boolean> PERSIST_BACK_LOCATION                                  = new Option<>("persist_back_location", false, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> RECHECK_PLAYER_ABILITY_PERMISSIONS_ON_DIMENSION_CHANGE = new Option<>("recheck_player_ability_permissions_on_dimension_change", false, Boolean::parseBoolean);

    protected MiscConfig(String name) {
        super(name);
        HOME_LIMIT.changeEvent.register(newValue ->
            ECPerms.Registry.Group.home_limit_group = ECPerms.makeNumericPermissionGroup("essentialcommands.home.limit", newValue)
        );
    }
}
