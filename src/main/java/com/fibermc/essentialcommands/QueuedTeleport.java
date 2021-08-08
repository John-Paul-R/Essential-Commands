package com.fibermc.essentialcommands;

import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.types.MinecraftLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;

public abstract class QueuedTeleport {

    private int ticksRemaining;
    private final PlayerData playerData;
    private final Text destName;


    public QueuedTeleport(PlayerData playerData, Text destName) {
        this.playerData = playerData;
        this.destName = destName;
        this.ticksRemaining = (int)(CONFIG.TELEPORT_DELAY.getValue()*20);
    }

    public QueuedTeleport(PlayerData playerData, Text destName, int delay) {
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

    public MutableText getDestName() {
        return (MutableText) destName;
    }

    public PlayerData getPlayerData() {
        return playerData;
    }

    public void complete() {
        ((ServerPlayerEntityAccess) playerData.getPlayer()).endEcQueuedTeleport();
    }
}
