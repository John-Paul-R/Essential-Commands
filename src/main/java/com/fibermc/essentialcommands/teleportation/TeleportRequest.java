package com.fibermc.essentialcommands.teleportation;

import com.fibermc.essentialcommands.EssentialCommands;
import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.playerdata.PlayerData;

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

    public final Type type;
    private final PlayerData senderPlayer;
    private final PlayerData targetPlayer;
    private boolean isEnded = false;
    private int ageTicks = 0;

    public TeleportRequest(ServerPlayerEntity senderPlayer, ServerPlayerEntity targetPlayer, Type requestType) {
        this.type = requestType;
        this.senderPlayer = ((ServerPlayerEntityAccess) senderPlayer).ec$getPlayerData();
        this.targetPlayer = ((ServerPlayerEntityAccess) targetPlayer).ec$getPlayerData();
    }

    public void queue() {
        ServerPlayerEntity teleportee;
        ServerPlayerEntity tpDestination;

        if (type == Type.TPA_HERE) {
            tpDestination = senderPlayer.getPlayer();
            teleportee = targetPlayer.getPlayer();
        } else if (type == Type.TPA_TO) {
            tpDestination = targetPlayer.getPlayer();
            teleportee = senderPlayer.getPlayer();
        } else {
            EssentialCommands.LOGGER.warn(String.format("Invalid teleport request type %s", type.toString()));
            return;
        }

        PlayerTeleporter.requestTeleport(new QueuedPlayerTeleport(teleportee, tpDestination));
    }

    public void incrementAgeTicks() {
        ageTicks++;
    }

    public int getAgeTicks() {
        return ageTicks;
    }

    public void end() {
        var targetPlayerData = this.getTargetPlayerData();
        if (targetPlayerData != null) {
            targetPlayerData.removeIncomingTeleportRequest(this.getSenderPlayer().getUuid());
            this.getSenderPlayerData().removeSentTeleportRequest(this);
        }
        isEnded = true;
    }

    public boolean isEnded() {
        return isEnded;
    }

}
