package com.fibermc.essentialcommands.access;

import com.fibermc.essentialcommands.PlayerData;

public interface PlayerEntityAccess {
    PlayerData getEcPlayerData();

    void setEcPlayerData(PlayerData playerData);
}
