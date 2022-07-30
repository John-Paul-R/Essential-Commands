package com.fibermc.essentialcommands.teleportation;

import com.fibermc.essentialcommands.playerdata.PlayerData;
import com.fibermc.essentialcommands.types.MinecraftLocation;

import net.minecraft.text.MutableText;

public class QueuedLocationTeleport extends QueuedTeleport {

    private final MinecraftLocation dest;

    public QueuedLocationTeleport(PlayerData playerData, MinecraftLocation dest, MutableText destName) {
        super(playerData, destName);
        this.dest = dest;
    }

    public QueuedLocationTeleport(PlayerData playerData, MinecraftLocation dest, MutableText destName, int delay) {
        super(playerData, destName, delay);
        this.dest = dest;
    }

    public MinecraftLocation getDest() {
        return dest;
    }
}
