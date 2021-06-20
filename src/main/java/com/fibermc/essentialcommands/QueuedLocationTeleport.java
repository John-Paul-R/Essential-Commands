package com.fibermc.essentialcommands;

import com.fibermc.essentialcommands.types.MinecraftLocation;

public class QueuedLocationTeleport extends QueuedTeleport {

    private final MinecraftLocation dest;

    public QueuedLocationTeleport(PlayerData playerData, MinecraftLocation dest, String destName) {
        super(playerData, destName);
        this.dest = dest;
    }
    public QueuedLocationTeleport(PlayerData playerData, MinecraftLocation dest, String destName, int delay) {
        super(playerData, destName, delay);
        this.dest = dest;
    }

    public MinecraftLocation getDest() {
        return dest;
    }
}
