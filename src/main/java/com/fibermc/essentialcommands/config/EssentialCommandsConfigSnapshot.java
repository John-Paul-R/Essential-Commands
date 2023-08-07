package com.fibermc.essentialcommands.config;

import java.util.List;
import java.util.Set;

import com.fibermc.essentialcommands.types.RespawnCondition;

import net.minecraft.registry.RegistryKey;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.world.World;

import dev.jpcode.eccore.config.expression.Expression;
import dev.jpcode.eccore.util.TimeUtil;

@SuppressWarnings("checkstyle:all")
public final class EssentialCommandsConfigSnapshot {

    public final Style FORMATTING_DEFAULT;
    public final Style FORMATTING_ACCENT;
    public final Style FORMATTING_ERROR;
    public final Text NICKNAME_PREFIX;
    public final boolean ENABLE_BACK;
    public final boolean ENABLE_HOME;
    public final boolean ENABLE_SPAWN;
    public final boolean ENABLE_TPA;
    public final boolean ENABLE_WARP;
    public final boolean ENABLE_NICK;
    public final boolean ENABLE_RTP;
    public final boolean ENABLE_FLY;
    public final boolean ENABLE_INVULN;
    public final boolean ENABLE_WORKBENCH;
    public final boolean ENABLE_ANVIL;
    public final boolean ENABLE_ENDERCHEST;
    public final boolean ENABLE_WASTEBIN;
    public final boolean ENABLE_ESSENTIALSX_CONVERT;
    public final boolean ENABLE_TOP;
    public final boolean ENABLE_GAMETIME;
    public final boolean ENABLE_MOTD;
    public final boolean ENABLE_AFK;
    public final boolean ENABLE_DAY;
    public final boolean ENABLE_RULES;
    public final boolean ENABLE_BED;
    public final List<Integer> HOME_LIMIT;
    public final double TELEPORT_COOLDOWN;
    public final int TELEPORT_DELAY_TICKS;
    public final boolean ALLOW_BACK_ON_DEATH;
    public final int TELEPORT_REQUEST_DURATION_TICKS;
    public final boolean USE_PERMISSIONS_API;
    public final boolean CHECK_FOR_UPDATES;
    public final boolean TELEPORT_INTERRUPT_ON_DAMAGED;
    public final boolean TELEPORT_INTERRUPT_ON_MOVE;
    public final double TELEPORT_INTERRUPT_ON_MOVE_AMOUNT;
    public final boolean ALLOW_TELEPORT_BETWEEN_DIMENSIONS;
    public final boolean OPS_BYPASS_TELEPORT_RULES;
    public final boolean NICKNAMES_IN_PLAYER_LIST;
    public final int NICKNAME_MAX_LENGTH;
    public final boolean NICKNAME_ABOVE_HEAD;
    public final int RTP_RADIUS;
    public final int RTP_MIN_RADIUS;
    public final int RTP_COOLDOWN;
    public final int RTP_MAX_ATTEMPTS;
    public final Set<RegistryKey<World>> RTP_ENABLED_WORLDS;
    public final boolean BROADCAST_TO_OPS;
    public final boolean NICK_REVEAL_ON_HOVER;
    public final boolean GRANT_LOWEST_NUMERIC_BY_DEFAULT;
    public final String LANGUAGE;
    public final String MOTD;
    public final Text AFK_PREFIX;
    public final boolean INVULN_WHILE_AFK;
    public final boolean AUTO_AFK_ENABLED;
    public final int AUTO_AFK_TICKS;
    public final boolean REGISTER_TOP_LEVEL_COMMANDS;
    public final List<String> EXCLUDED_TOP_LEVEL_COMMANDS;
    public final Expression<RespawnCondition> RESPAWN_AT_EC_SPAWN;
    public final boolean PERSIST_BACK_LOCATION;
    public final boolean RECHECK_PLAYER_ABILITY_PERMISSIONS_ON_DIMENSION_CHANGE;

    private EssentialCommandsConfigSnapshot(EssentialCommandsConfig config) {
        this.FORMATTING_DEFAULT                 = config.TEXT.FORMATTING_DEFAULT.getValue();
        this.FORMATTING_ACCENT                  = config.TEXT.FORMATTING_ACCENT.getValue();
        this.FORMATTING_ERROR                   = config.TEXT.FORMATTING_ERROR.getValue();
        this.NICKNAME_PREFIX                    = config.NICKNAME.NICKNAME_PREFIX.getValue();
        this.ENABLE_BACK                        = config.FEATURE_TOGGLES.ENABLE_BACK.getValue();
        this.ENABLE_HOME                        = config.FEATURE_TOGGLES.ENABLE_HOME.getValue();
        this.ENABLE_SPAWN                       = config.FEATURE_TOGGLES.ENABLE_SPAWN.getValue();
        this.ENABLE_TPA                         = config.FEATURE_TOGGLES.ENABLE_TPA.getValue();
        this.ENABLE_WARP                        = config.FEATURE_TOGGLES.ENABLE_WARP.getValue();
        this.ENABLE_NICK                        = config.FEATURE_TOGGLES.ENABLE_NICK.getValue();
        this.ENABLE_RTP                         = config.FEATURE_TOGGLES.ENABLE_RTP.getValue();
        this.ENABLE_FLY                         = config.FEATURE_TOGGLES.ENABLE_FLY.getValue();
        this.ENABLE_INVULN                      = config.FEATURE_TOGGLES.ENABLE_INVULN.getValue();
        this.ENABLE_WORKBENCH                   = config.FEATURE_TOGGLES.ENABLE_WORKBENCH.getValue();
        this.ENABLE_ANVIL                       = config.FEATURE_TOGGLES.ENABLE_ANVIL.getValue();
        this.ENABLE_ENDERCHEST                  = config.FEATURE_TOGGLES.ENABLE_ENDERCHEST.getValue();
        this.ENABLE_WASTEBIN                    = config.FEATURE_TOGGLES.ENABLE_WASTEBIN.getValue();
        this.ENABLE_ESSENTIALSX_CONVERT         = config.FEATURE_TOGGLES.ENABLE_ESSENTIALSX_CONVERT.getValue();
        this.ENABLE_TOP                         = config.FEATURE_TOGGLES.ENABLE_TOP.getValue();
        this.ENABLE_GAMETIME                    = config.FEATURE_TOGGLES.ENABLE_GAMETIME.getValue();
        this.ENABLE_MOTD                        = config.FEATURE_TOGGLES.ENABLE_MOTD.getValue();
        this.ENABLE_AFK                         = config.FEATURE_TOGGLES.ENABLE_AFK.getValue();
        this.ENABLE_DAY                         = config.FEATURE_TOGGLES.ENABLE_DAY.getValue();
        this.ENABLE_RULES                       = config.FEATURE_TOGGLES.ENABLE_RULES.getValue();
        this.ENABLE_BED                         = config.FEATURE_TOGGLES.ENABLE_BED.getValue();
        this.HOME_LIMIT                         = config.MISC.HOME_LIMIT.getValue();
        this.TELEPORT_COOLDOWN                  = config.TELEPORT.TELEPORT_COOLDOWN.getValue();
        this.TELEPORT_DELAY_TICKS               = (int) (config.TELEPORT.TELEPORT_DELAY.getValue() * TimeUtil.TPS);
        this.ALLOW_BACK_ON_DEATH                = config.MISC.ALLOW_BACK_ON_DEATH.getValue();
        this.TELEPORT_REQUEST_DURATION_TICKS    = config.MISC.TELEPORT_REQUEST_DURATION.getValue() * TimeUtil.TPS;
        this.USE_PERMISSIONS_API                = config.MISC.USE_PERMISSIONS_API.getValue();
        this.CHECK_FOR_UPDATES                  = config.MISC.CHECK_FOR_UPDATES.getValue();
        this.TELEPORT_INTERRUPT_ON_DAMAGED      = config.TELEPORT.TELEPORT_INTERRUPT_ON_DAMAGED.getValue();
        this.TELEPORT_INTERRUPT_ON_MOVE         = config.TELEPORT.TELEPORT_INTERRUPT_ON_MOVE.getValue();
        this.TELEPORT_INTERRUPT_ON_MOVE_AMOUNT  = config.TELEPORT.TELEPORT_INTERRUPT_ON_MOVE_AMOUNT.getValue();
        this.ALLOW_TELEPORT_BETWEEN_DIMENSIONS  = config.TELEPORT.ALLOW_TELEPORT_BETWEEN_DIMENSIONS.getValue();
        this.OPS_BYPASS_TELEPORT_RULES          = config.TELEPORT.OPS_BYPASS_TELEPORT_RULES.getValue();
        this.NICKNAMES_IN_PLAYER_LIST           = config.NICKNAME.NICKNAMES_IN_PLAYER_LIST.getValue();
        this.NICKNAME_MAX_LENGTH                = config.NICKNAME.NICKNAME_MAX_LENGTH.getValue();
        this.NICKNAME_ABOVE_HEAD                = config.NICKNAME.NICKNAME_ABOVE_HEAD.getValue();
        this.RTP_RADIUS                         = config.RTP.RTP_RADIUS.getValue();
        this.RTP_MIN_RADIUS                     = config.RTP.RTP_MIN_RADIUS.getValue();
        this.RTP_COOLDOWN                       = config.RTP.RTP_COOLDOWN.getValue();
        this.RTP_MAX_ATTEMPTS                   = config.RTP.RTP_MAX_ATTEMPTS.getValue();
        this.RTP_ENABLED_WORLDS                 = config.RTP.getValidRtpWorldKeys();
        this.BROADCAST_TO_OPS                   = config.MISC.BROADCAST_TO_OPS.getValue();
        this.NICK_REVEAL_ON_HOVER               = config.NICKNAME.NICK_REVEAL_ON_HOVER.getValue();
        this.GRANT_LOWEST_NUMERIC_BY_DEFAULT    = config.MISC.GRANT_LOWEST_NUMERIC_BY_DEFAULT.getValue();
        this.LANGUAGE                           = config.MISC.LANGUAGE.getValue();
        this.MOTD                               = config.MISC.MOTD.getValue();
        this.AFK_PREFIX                         = config.AFK.AFK_PREFIX.getValue();
        this.INVULN_WHILE_AFK                   = config.AFK.INVULN_WHILE_AFK.getValue();
        this.AUTO_AFK_ENABLED                   = config.AFK.AUTO_AFK_ENABLED.getValue();
        this.AUTO_AFK_TICKS                     = config.AFK.AUTO_AFK_TICKS.getValue();
        this.REGISTER_TOP_LEVEL_COMMANDS        = config.MISC.REGISTER_TOP_LEVEL_COMMANDS.getValue();
        this.EXCLUDED_TOP_LEVEL_COMMANDS        = config.MISC.EXCLUDED_TOP_LEVEL_COMMANDS.getValue();
        this.RESPAWN_AT_EC_SPAWN                = config.MISC.RESPAWN_AT_EC_SPAWN.getValue();
        this.PERSIST_BACK_LOCATION              = config.MISC.PERSIST_BACK_LOCATION.getValue();
        this.RECHECK_PLAYER_ABILITY_PERMISSIONS_ON_DIMENSION_CHANGE = config.MISC.RECHECK_PLAYER_ABILITY_PERMISSIONS_ON_DIMENSION_CHANGE.getValue();
    }

    public static EssentialCommandsConfigSnapshot create(EssentialCommandsConfig config) {
        return new EssentialCommandsConfigSnapshot(config);
    }
}
