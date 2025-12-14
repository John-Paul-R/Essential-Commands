package com.fibermc.essentialcommands.teleportation;

import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.playerdata.PlayerData;
import com.fibermc.essentialcommands.types.MinecraftLocation;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class QueuedPlayerTeleport extends QueuedTeleport {

    private final ServerPlayer targetPlayer;

    public QueuedPlayerTeleport(PlayerData playerData, ServerPlayer targetPlayer, Component destName) {
        super(playerData, destName);
        this.targetPlayer = targetPlayer;
    }

    public QueuedPlayerTeleport(PlayerData playerData, ServerPlayer targetPlayer, Component destName, int delay) {
        super(playerData, destName, delay);
        this.targetPlayer = targetPlayer;
    }

    public QueuedPlayerTeleport(ServerPlayer teleportingPlayer, ServerPlayer destinationPlayer) {
        super(((ServerPlayerEntityAccess) teleportingPlayer).ec$getPlayerData(), destinationPlayer.getDisplayName());
        this.targetPlayer = destinationPlayer;
    }

    public MinecraftLocation getDest() {
        return new MinecraftLocation(targetPlayer);
    }
}
