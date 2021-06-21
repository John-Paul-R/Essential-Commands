package com.fibermc.essentialcommands;

import com.fibermc.essentialcommands.QueuedTeleport;

public interface ServerPlayerEntityAccess {
    QueuedTeleport getEcQueuedTeleport();

    void setEcQueuedTeleport(QueuedTeleport queuedTeleport);

    QueuedTeleport endEcQueuedTeleport();
}
