package com.fibermc.essentialcommands;

import com.fibermc.essentialcommands.types.MinecraftLocation;
import net.minecraft.server.MinecraftServer;

public class QueuedTeleport {

    private int ticksRemaining;
    private final MinecraftLocation dest;
    private final String destName;
    private final PlayerData playerData;

    public QueuedTeleport(PlayerData playerData, MinecraftLocation dest, String destName, int delay) {
        this.playerData = playerData;
        this.dest = dest;
        this.destName = destName;
        this.ticksRemaining = delay;
    }

    public int getTicksRemaining() {
        return ticksRemaining;
    }

    public void tick(MinecraftServer server) {
        this.ticksRemaining--;
    }

    public MinecraftLocation getDest() {
        return dest;
    }

    public String getDestName() {
        return destName;
    }

    public PlayerData getPlayerData() {
        return playerData;
    }

}
