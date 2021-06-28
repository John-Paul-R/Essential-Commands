package com.fibermc.essentialcommands;

import com.fibermc.essentialcommands.access.PlayerEntityAccess;
import com.fibermc.essentialcommands.events.PlayerConnectCallback;
import com.fibermc.essentialcommands.events.PlayerDeathCallback;
import com.fibermc.essentialcommands.events.PlayerLeaveCallback;
import com.fibermc.essentialcommands.events.PlayerRespawnCallback;
import com.fibermc.essentialcommands.types.MinecraftLocation;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataManager {

    private final ConcurrentHashMap<UUID, PlayerData> dataMap;

    private static PlayerDataManager INSTANCE;

    public PlayerDataManager() {
        INSTANCE = this;
        this.dataMap = new ConcurrentHashMap<>();
    }

    public static void init() {
//        PlayerConnectCallback.EVENT.register(PlayerDataManager::onPlayerConnect);
        PlayerLeaveCallback.EVENT.register(PlayerDataManager::onPlayerLeave);
        PlayerDeathCallback.EVENT.register(PlayerDataManager::onPlayerDeath);
        PlayerRespawnCallback.EVENT.register(PlayerDataManager::onPlayerRespawn);
    }

    public static PlayerDataManager getInstance() {
        return INSTANCE;
    }

    // EVENTS
    public static void onPlayerConnect(ClientConnection connection, ServerPlayerEntity player) {
        PlayerData playerData = INSTANCE.addPlayerData(player);
        ((PlayerEntityAccess) player).setEcPlayerData(playerData);
        INSTANCE.initPlayerDataFile(player);
    }

    public static void onPlayerLeave(ServerPlayerEntity player) {
        // Auto-saving should be handled by WorldSaveHandlerMixin. (PlayerData saves when MC server saves players)
        INSTANCE.unloadPlayerData(player);
    }

    private static void onPlayerRespawn(ServerPlayerEntity serverPlayerEntity) {
        PlayerData pData = ((PlayerEntityAccess) serverPlayerEntity).getEcPlayerData();
        pData.updatePlayer(serverPlayerEntity);
        ((PlayerEntityAccess) serverPlayerEntity).setEcPlayerData(pData);
    }

    private static void onPlayerDeath(ServerPlayerEntity playerEntity, DamageSource damageSource) {
        PlayerData pData = ((PlayerEntityAccess) playerEntity).getEcPlayerData();
        if (Config.ALLOW_BACK_ON_DEATH)
            pData.setPreviousLocation(new MinecraftLocation(pData.getPlayer()));
    }

    // SET / ADD
    public PlayerData addPlayerData(ServerPlayerEntity player) {
        PlayerData playerData = PlayerDataFactory.create(player);
        dataMap.put(player.getUuid(), playerData);
        return playerData;
    }

    public PlayerData getPlayerData(ServerPlayerEntity player) {
        PlayerData playerData = dataMap.get(player.getUuid());

        if (playerData == null) {
            throw new NullPointerException(String.format("dataMap returned null for player with uuid %s", player.getUuid().toString()));
        }
        return playerData;
    }
    PlayerData getPlayerFromUUID(UUID playerID) {
        return dataMap.get(playerID);
    }

    // SAVE / LOAD
    private void unloadPlayerData(ServerPlayerEntity player) {
        this.dataMap.remove(player.getUuid());
    }

    private void initPlayerDataFile(ServerPlayerEntity player) {
        PlayerData pData = this.getPlayerData(player);
        pData.markDirty();
        pData.save();
    }

}