package com.fibermc.essentialcommands;

import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.types.MinecraftLocation;
import net.minecraft.server.MinecraftServer;

public abstract class QueuedTeleport {

    private int ticksRemaining;
    private final PlayerData playerData;
    private final String destName;


    public QueuedTeleport(PlayerData playerData, String destName) {
        this.playerData = playerData;
        this.destName = destName;
        this.ticksRemaining = (int)(Config.TELEPORT_DELAY*20);
    }

    public QueuedTeleport(PlayerData playerData, String destName, int delay) {
        this.playerData = playerData;
        this.destName = destName;
        this.ticksRemaining = delay;
    }

    public int getTicksRemaining() {
        return ticksRemaining;
    }

    public void tick(MinecraftServer server) {
        this.ticksRemaining--;
    }

    public abstract MinecraftLocation getDest();

    public String getDestName() {
        return destName;
    }

    public PlayerData getPlayerData() {
        return playerData;
    }

    public void complete() {
        ((ServerPlayerEntityAccess) playerData.getPlayer()).endEcQueuedTeleport();
    }
}
