package com.fibermc.essentialcommands.access;

import com.fibermc.essentialcommands.playerdata.PlayerData;
import com.fibermc.essentialcommands.playerdata.PlayerProfile;
import com.fibermc.essentialcommands.teleportation.QueuedTeleport;
import com.fibermc.essentialcommands.text.ECText;

public interface ServerPlayerEntityAccess {
    QueuedTeleport ec$getQueuedTeleport();

    void ec$setQueuedTeleport(QueuedTeleport queuedTeleport);

    QueuedTeleport ec$endQueuedTeleport();

    PlayerData ec$getPlayerData();

    void ec$setPlayerData(PlayerData playerData);

    PlayerProfile ec$getProfile();

    void ec$setProfile(PlayerProfile profile);

    ECText ec$getEcText();
}
