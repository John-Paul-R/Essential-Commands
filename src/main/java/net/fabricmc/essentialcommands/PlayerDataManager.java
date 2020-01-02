package net.fabricmc.essentialcommands;


import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.network.ClientConnection;
import net.minecraft.server.network.ServerPlayerEntity;

public class PlayerDataManager {

    private ConcurrentHashMap<UUID, PlayerData> dataMap;
    
    public PlayerDataManager() {
        this.dataMap = new ConcurrentHashMap<UUID, PlayerData>();
    }

    public void addPlayerData(ServerPlayerEntity player) {
        
        dataMap.put(player.getUuid(), new PlayerData(player.getUuidAsString(), player)); 
    }

    PlayerData getOrCreate(ServerPlayerEntity player) {
        
        UUID uuid = player.getUuid();
        if (!dataMap.containsKey(uuid)) {
            addPlayerData(player);
        }
        return dataMap.get(uuid);
    }

    ConcurrentHashMap<UUID, PlayerData> getDataMap() {
        return this.dataMap;
    }

    public void onPlayerConnect(ClientConnection connection, ServerPlayerEntity player) {
        System.out.println("Connection Test - JP");
    }
    //-=-=-=-=-=-=-=-=-=-=-=-



    
}