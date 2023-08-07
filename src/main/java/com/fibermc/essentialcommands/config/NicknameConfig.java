package com.fibermc.essentialcommands.config;

import com.fibermc.essentialcommands.playerdata.PlayerDataManager;

import net.minecraft.text.Text;

import dev.jpcode.eccore.config.ConfigOption;
import dev.jpcode.eccore.config.ConfigSectionSkeleton;
import dev.jpcode.eccore.config.ConfigUtil;
import dev.jpcode.eccore.config.Option;
import dev.jpcode.eccore.util.TextUtil;

import static dev.jpcode.eccore.util.TextUtil.parseText;

public class NicknameConfig extends ConfigSectionSkeleton {
    @ConfigOption public final Option<Text> NICKNAME_PREFIX                                          = new Option<>("nickname_prefix", parseText("{\"text\":\"~\",\"color\":\"red\"}"), TextUtil::parseText, Text.Serializer::toJson);
    @ConfigOption public final Option<Boolean> NICKNAMES_IN_PLAYER_LIST                               = new Option<>("nicknames_in_player_list", true, Boolean::parseBoolean);
    @ConfigOption public final Option<Integer> NICKNAME_MAX_LENGTH                                    = new Option<>("nickname_max_length", 32, ConfigUtil::parseInt);
    @ConfigOption public final Option<Boolean> NICKNAME_ABOVE_HEAD                                    = new Option<>("nickname_above_head", false, Boolean::parseBoolean);
    @ConfigOption public final Option<Boolean> NICK_REVEAL_ON_HOVER                                   = new Option<>("nick_reveal_on_hover", true, Boolean::parseBoolean);

    public NicknameConfig(String name) {
        super(name);
        NICKNAMES_IN_PLAYER_LIST.changeEvent.register(ign -> {
            PlayerDataManager.getInstance().queueNicknameUpdatesForAllPlayers();
        });
    }
}
