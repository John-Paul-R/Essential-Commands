package com.fibermc.essentialcommands.config;

import dev.jpcode.eccore.config.ConfigOption;
import dev.jpcode.eccore.config.ConfigSectionSkeleton;
import dev.jpcode.eccore.config.ConfigUtil;
import dev.jpcode.eccore.config.Option;

public class TeleportConfig extends ConfigSectionSkeleton {
    @ConfigOption public final Option<Double> TELEPORT_COOLDOWN                                      = new Option<>("teleport_cooldown", 1.0, ConfigUtil::parseDouble);
    @ConfigOption public final Option<Double>  TELEPORT_DELAY                                         = new Option<>("teleport_delay", 0.0, ConfigUtil::parseDouble);
    @ConfigOption public final Option<Boolean> TELEPORT_INTERRUPT_ON_DAMAGED                          = new Option<>("teleport_interrupt_on_damaged", true, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> TELEPORT_INTERRUPT_ON_MOVE                             = new Option<>("teleport_interrupt_on_move", false, Boolean::parseBoolean);
    @ConfigOption public final Option<Double>  TELEPORT_INTERRUPT_ON_MOVE_AMOUNT                      = new Option<>("teleport_interrupt_on_move_max_blocks", 3D, ConfigUtil::parseDouble);
    @ConfigOption public final Option<Boolean> ALLOW_TELEPORT_BETWEEN_DIMENSIONS                      = new Option<>("allow_teleport_between_dimensions", true, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> OPS_BYPASS_TELEPORT_RULES                = new Option<>("ops_bypass_teleport_rules", true, Boolean::parseBoolean);

    protected TeleportConfig(String name) {
        super(name);
    }
}
