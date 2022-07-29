package com.fibermc.essentialcommands.access;

import com.fibermc.essentialcommands.PlayerData;
import com.fibermc.essentialcommands.PlayerProfile;
import com.fibermc.essentialcommands.QueuedTeleport;

public interface ServerPlayerEntityAccess {
    QueuedTeleport ec$getQueuedTeleport();

    void ec$setQueuedTeleport(QueuedTeleport queuedTeleport);

    QueuedTeleport ec$endQueuedTeleport();

    PlayerData ec$getPlayerData();

    void ec$setPlayerData(PlayerData playerData);

    PlayerProfile ec$getProfile();

    void ec$setProfile(PlayerProfile profile);
}
