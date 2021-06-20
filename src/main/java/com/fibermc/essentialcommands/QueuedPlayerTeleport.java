package com.fibermc.essentialcommands;

import com.fibermc.essentialcommands.types.MinecraftLocation;
import net.minecraft.server.network.ServerPlayerEntity;

public class QueuedPlayerTeleport extends QueuedTeleport {

    private final ServerPlayerEntity targetPlayer;

    public QueuedPlayerTeleport(PlayerData playerData, ServerPlayerEntity targetPlayer, String destName) {
        super(playerData, destName);
        this.targetPlayer = targetPlayer;
    }

    public QueuedPlayerTeleport(PlayerData playerData, ServerPlayerEntity targetPlayer, String destName, int delay) {
        super(playerData, destName, delay);
        this.targetPlayer = targetPlayer;
    }

    public MinecraftLocation getDest() {
        return new MinecraftLocation(targetPlayer);
    }
}
