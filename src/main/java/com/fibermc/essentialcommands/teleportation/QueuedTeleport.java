package com.fibermc.essentialcommands.teleportation;

import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.playerdata.PlayerData;
import com.fibermc.essentialcommands.types.MinecraftLocation;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.phys.Vec3;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;

public abstract class QueuedTeleport {

    private int ticksRemaining;
    private final PlayerData playerData;
    private final Component destName;
    public final Vec3 initialPosition;

    public QueuedTeleport(PlayerData playerData, Component destName) {
        this.playerData = playerData;
        this.destName = destName;
        this.ticksRemaining = CONFIG.TELEPORT_DELAY_TICKS;
        this.initialPosition = playerData.getPlayer().position();
    }

    public QueuedTeleport(PlayerData playerData, Component destName, int delay) {
        this.playerData = playerData;
        this.destName = destName;
        this.ticksRemaining = delay;
        this.initialPosition = playerData.getPlayer().position();
    }

    public int getTicksRemaining() {
        return ticksRemaining;
    }

    public void tick(MinecraftServer server) {
        this.ticksRemaining--;
    }

    public abstract MinecraftLocation getDest();

    public MutableComponent getDestName() {
        return (MutableComponent) destName;
    }

    public PlayerData getPlayerData() {
        return playerData;
    }

    public void complete() {
        ((ServerPlayerEntityAccess) playerData.getPlayer()).ec$endQueuedTeleport();
    }
}
