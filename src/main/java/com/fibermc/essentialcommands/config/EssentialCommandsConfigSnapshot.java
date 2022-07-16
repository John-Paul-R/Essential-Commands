package com.fibermc.essentialcommands.config;

import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.List;

public final class EssentialCommandsConfigSnapshot {

    public final boolean ENABLE_DAY;
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
    public final List<Integer> HOME_LIMIT;
    public final double TELEPORT_COOLDOWN;
    public final double TELEPORT_DELAY;
    public final boolean ALLOW_BACK_ON_DEATH;
    public final int TELEPORT_REQUEST_DURATION;
    public final boolean USE_PERMISSIONS_API;
    public final boolean CHECK_FOR_UPDATES;
    public final boolean TELEPORT_INTERRUPT_ON_DAMAGED;
    public final boolean TELEPORT_INTERRUPT_ON_MOVE;
    public final double TELEPORT_INTERRUPT_ON_MOVE_AMOUNT;
    public final boolean ALLOW_TELEPORT_BETWEEN_DIMENSIONS;
    public final boolean OPS_BYPASS_TELEPORT_RULES;
    public final boolean NICKNAMES_IN_PLAYER_LIST;
    public final int NICKNAME_MAX_LENGTH;
    public final int RTP_RADIUS;
    public final int RTP_MIN_RADIUS;
    public final int RTP_COOLDOWN;
    public final int RTP_MAX_ATTEMPTS;
    public final boolean BROADCAST_TO_OPS;
    public final boolean NICK_REVEAL_ON_HOVER;
    public final boolean GRANT_LOWEST_NUMERIC_BY_DEFAULT;
    public final String LANGUAGE;
    public final String MOTD;
    public final Text AFK_PREFIX;
    public final boolean INVULN_WHILE_AFK;
    public final boolean AUTO_AFK_ENABLED;
    public final int AUTO_AFK_TICKS;

    private EssentialCommandsConfigSnapshot(EssentialCommandsConfig config) {
        this.FORMATTING_DEFAULT                = config.FORMATTING_DEFAULT.getValue();
        this.FORMATTING_ACCENT                 = config.FORMATTING_ACCENT.getValue();
        this.FORMATTING_ERROR                  = config.FORMATTING_ERROR.getValue();
        this.NICKNAME_PREFIX                   = config.NICKNAME_PREFIX.getValue();
        this.ENABLE_BACK                       = config.ENABLE_BACK.getValue();
        this.ENABLE_HOME                       = config.ENABLE_HOME.getValue();
        this.ENABLE_SPAWN                      = config.ENABLE_SPAWN.getValue();
        this.ENABLE_TPA                        = config.ENABLE_TPA.getValue();
        this.ENABLE_WARP                       = config.ENABLE_WARP.getValue();
        this.ENABLE_NICK                       = config.ENABLE_NICK.getValue();
        this.ENABLE_RTP                        = config.ENABLE_RTP.getValue();
        this.ENABLE_FLY                        = config.ENABLE_FLY.getValue();
        this.ENABLE_INVULN                     = config.ENABLE_INVULN.getValue();
        this.ENABLE_WORKBENCH                  = config.ENABLE_WORKBENCH.getValue();
        this.ENABLE_ANVIL                      = config.ENABLE_ANVIL.getValue();
        this.ENABLE_ENDERCHEST                 = config.ENABLE_ENDERCHEST.getValue();
        this.ENABLE_WASTEBIN                   = config.ENABLE_WASTEBIN.getValue();
        this.ENABLE_ESSENTIALSX_CONVERT        = config.ENABLE_ESSENTIALSX_CONVERT.getValue();
        this.ENABLE_TOP                        = config.ENABLE_TOP.getValue();
        this.ENABLE_GAMETIME                   = config.ENABLE_GAMETIME.getValue();
        this.ENABLE_MOTD                       = config.ENABLE_MOTD.getValue();
        this.ENABLE_AFK                        = config.ENABLE_AFK.getValue();
        this.ENABLE_DAY                        = config.ENABLE_DAY.getValue();
        this.HOME_LIMIT                        = config.HOME_LIMIT.getValue();
        this.TELEPORT_COOLDOWN                 = config.TELEPORT_COOLDOWN.getValue();
        this.TELEPORT_DELAY                    = config.TELEPORT_DELAY.getValue();
        this.ALLOW_BACK_ON_DEATH               = config.ALLOW_BACK_ON_DEATH.getValue();
        this.TELEPORT_REQUEST_DURATION         = config.TELEPORT_REQUEST_DURATION.getValue();
        this.USE_PERMISSIONS_API               = config.USE_PERMISSIONS_API.getValue();
        this.CHECK_FOR_UPDATES                 = config.CHECK_FOR_UPDATES.getValue();
        this.TELEPORT_INTERRUPT_ON_DAMAGED     = config.TELEPORT_INTERRUPT_ON_DAMAGED.getValue();
        this.TELEPORT_INTERRUPT_ON_MOVE        = config.TELEPORT_INTERRUPT_ON_MOVE.getValue();
        this.TELEPORT_INTERRUPT_ON_MOVE_AMOUNT = config.TELEPORT_INTERRUPT_ON_MOVE_AMOUNT.getValue();
        this.ALLOW_TELEPORT_BETWEEN_DIMENSIONS = config.ALLOW_TELEPORT_BETWEEN_DIMENSIONS.getValue();
        this.OPS_BYPASS_TELEPORT_RULES         = config.OPS_BYPASS_TELEPORT_RULES.getValue();
        this.NICKNAMES_IN_PLAYER_LIST          = config.NICKNAMES_IN_PLAYER_LIST.getValue();
        this.NICKNAME_MAX_LENGTH               = config.NICKNAME_MAX_LENGTH.getValue();
        this.RTP_RADIUS                        = config.RTP_RADIUS.getValue();
        this.RTP_MIN_RADIUS                    = config.RTP_MIN_RADIUS.getValue();
        this.RTP_COOLDOWN                      = config.RTP_COOLDOWN.getValue();
        this.RTP_MAX_ATTEMPTS                  = config.RTP_MAX_ATTEMPTS.getValue();
        this.BROADCAST_TO_OPS                  = config.BROADCAST_TO_OPS.getValue();
        this.NICK_REVEAL_ON_HOVER              = config.NICK_REVEAL_ON_HOVER.getValue();
        this.GRANT_LOWEST_NUMERIC_BY_DEFAULT   = config.GRANT_LOWEST_NUMERIC_BY_DEFAULT.getValue();
        this.LANGUAGE                          = config.LANGUAGE.getValue();
        this.MOTD                              = config.MOTD.getValue();
        this.AFK_PREFIX                        = config.AFK_PREFIX.getValue();
        this.INVULN_WHILE_AFK                  = config.INVULN_WHILE_AFK.getValue();
        this.AUTO_AFK_ENABLED                  = config.AUTO_AFK_ENABLED.getValue();
        this.AUTO_AFK_TICKS                    = config.AUTO_AFK_TICKS.getValue();
    }

    public static EssentialCommandsConfigSnapshot create(EssentialCommandsConfig config) {
        return new EssentialCommandsConfigSnapshot(config);
    }
}
