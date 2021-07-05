package com.fibermc.essentialcommands.access;

import com.fibermc.essentialcommands.PlayerData;
import com.fibermc.essentialcommands.QueuedTeleport;

public interface ServerPlayerEntityAccess {
    QueuedTeleport getEcQueuedTeleport();

    void setEcQueuedTeleport(QueuedTeleport queuedTeleport);

    QueuedTeleport endEcQueuedTeleport();

    PlayerData getEcPlayerData();

    void setEcPlayerData(PlayerData playerData);

}
