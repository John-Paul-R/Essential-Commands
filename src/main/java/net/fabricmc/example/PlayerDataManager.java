package net.fabricmc.example;

import java.util.LinkedList;
import java.util.ListIterator;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.server.network.ServerPlayerEntity;

public class PlayerDataManager {

    private ConcurrentHashMap<UUID, PlayerData> dataMap;
    private LinkedList<PlayerData> tpList;
    public PlayerDataManager() {
        this.dataMap = new ConcurrentHashMap<UUID, PlayerData>();
        tpList = new LinkedList<PlayerData>();
    }

    public void tick() {
        //decrement the tp timer for all players that have put in a tp request
        ListIterator<PlayerData> iter = tpList.listIterator();
        while (iter.hasNext()) {
            PlayerData e = iter.next();
            e.tickTpTimer();
            if (e.getTpTimer() == -1) {
                e.setTpTarget(null);
                iter.remove();
            }
        }
    }

    public PlayerData addPlayerData(ServerPlayerEntity player) {
        
        return dataMap.put(player.getUuid(), new PlayerData(player));
    }

    PlayerData getOrCreatePlayerData(ServerPlayerEntity player) {
        
        UUID uuid = player.getUuid();
        if (!dataMap.containsKey(uuid)) {
            addPlayerData(player);
        }
        
        return dataMap.get(uuid);
    }

    public void handleTpRequest(ServerPlayerEntity senderPlayer, ServerPlayerEntity tpTargetPlayer) {
        PlayerData data = getOrCreatePlayerData(senderPlayer);
        data.setTpTimer(60);
        data.setTpTarget(tpTargetPlayer);
        tpList.add(data);
        
    }
    
    public void handleTpResponse(ServerPlayerEntity senderPlayer, ServerPlayerEntity targetPlayer) {
        PlayerData data = getOrCreatePlayerData(targetPlayer);
        data.setTpTimer(-1);
        data.setTpTarget(null);
        tpList.remove(data);
        
    }

    ConcurrentHashMap<UUID, PlayerData> getDataMap() {
        return this.dataMap;
    }

    
}