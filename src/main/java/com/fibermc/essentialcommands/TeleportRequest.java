package com.fibermc.essentialcommands;

import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;

import net.minecraft.server.network.ServerPlayerEntity;

public class TeleportRequest {

    public ServerPlayerEntity getSenderPlayer() {
        return senderPlayer.getPlayer();
    }

    public ServerPlayerEntity getTargetPlayer() {
        return targetPlayer.getPlayer();
    }

    public PlayerData getSenderPlayerData() {
        return senderPlayer;
    }

    public PlayerData getTargetPlayerData() {
        return targetPlayer;
    }

    public enum Type {
        TPA_TO,
        TPA_HERE
    }

    private final Type requestType;
    private final PlayerData senderPlayer;
    private final PlayerData targetPlayer;
    private boolean isEnded = false;

    public TeleportRequest(ServerPlayerEntity senderPlayer, ServerPlayerEntity targetPlayer, Type requestType) {
        this.requestType = requestType;
        this.senderPlayer = ((ServerPlayerEntityAccess) senderPlayer).ec$getPlayerData();
        this.targetPlayer = ((ServerPlayerEntityAccess) targetPlayer).ec$getPlayerData();
    }

    public void queue() {
        ServerPlayerEntity teleportee;
        ServerPlayerEntity tpDestination;

        if (requestType == Type.TPA_HERE) {
            tpDestination = senderPlayer.getPlayer();
            teleportee = targetPlayer.getPlayer();
        } else if (requestType == Type.TPA_TO) {
            tpDestination = targetPlayer.getPlayer();
            teleportee = senderPlayer.getPlayer();
        } else {
            EssentialCommands.LOGGER.warn(String.format("Invalid teleport request type %s", requestType.toString()));
            return;
        }

        PlayerTeleporter.requestTeleport(new QueuedPlayerTeleport(teleportee, tpDestination));
    }

    public void end() {
        PlayerData target = this.getTargetPlayerData();
        if (target != null) {
            target.removeIncomingTeleportRequest(this.getSenderPlayer().getUuid());
            this.getSenderPlayerData().setSentTeleportRequest(null);
        }
        isEnded = true;
    }

    public boolean isEnded() {
        return isEnded;
    }

}
