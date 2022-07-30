package com.fibermc.essentialcommands.types;

import com.fibermc.essentialcommands.playerdata.PlayerProfile;

@FunctionalInterface
public interface ProfileOptionGetter<T> {
    T getValue(PlayerProfile profile);
}
