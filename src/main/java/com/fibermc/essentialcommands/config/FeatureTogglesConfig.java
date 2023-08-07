package com.fibermc.essentialcommands.config;

import dev.jpcode.eccore.config.ConfigOption;
import dev.jpcode.eccore.config.ConfigSectionSkeleton;
import dev.jpcode.eccore.config.Option;

public class FeatureTogglesConfig extends ConfigSectionSkeleton {
    @ConfigOption
    public final Option<Boolean> ENABLE_BACK                                            = new Option<>("enable_back", true, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> ENABLE_HOME                                            = new Option<>("enable_home", true, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> ENABLE_SPAWN                                           = new Option<>("enable_spawn", true, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> ENABLE_TPA                                             = new Option<>("enable_tpa", true, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> ENABLE_WARP                                            = new Option<>("enable_warp", true, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> ENABLE_NICK                                            = new Option<>("enable_nick", true, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> ENABLE_RTP                                             = new Option<>("enable_rtp", true, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> ENABLE_FLY                                             = new Option<>("enable_fly", true, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> ENABLE_INVULN                                          = new Option<>("enable_invuln", true, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> ENABLE_WORKBENCH                                       = new Option<>("enable_workbench", true, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> ENABLE_ANVIL                                           = new Option<>("enable_anvil", false, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> ENABLE_ENDERCHEST                                      = new Option<>("enable_enderchest", true, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> ENABLE_WASTEBIN                                        = new Option<>("enable_wastebin", true, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> ENABLE_ESSENTIALSX_CONVERT                             = new Option<>("enable_experimental_essentialsx_converter", false, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> ENABLE_TOP                                             = new Option<>("enable_top", true, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> ENABLE_GAMETIME                                        = new Option<>("enable_gametime", true, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> ENABLE_MOTD                                            = new Option<>("enable_motd", false, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> ENABLE_AFK                                             = new Option<>("enable_afk", true, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> ENABLE_DAY                                             = new Option<>("enable_day", true, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> ENABLE_RULES                                           = new Option<>("enable_rules", true, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> ENABLE_BED                                             = new Option<>("enable_bed", false, Boolean::parseBoolean);

    protected FeatureTogglesConfig(String name) {
        super(name);
    }
}
