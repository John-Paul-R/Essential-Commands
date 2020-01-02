package com.fibermc.essentialcommands;

import java.util.LinkedList;
import java.util.ListIterator;

import net.minecraft.server.network.ServerPlayerEntity;

/**
 * TeleportRequestManager
 */
public class TeleportRequestManager {

    private PlayerDataManager dataManager;
    private LinkedList<PlayerData> tpList;

    public TeleportRequestManager(PlayerDataManager dataManager) {
        this.dataManager = dataManager;
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

    // public List<PlayerData> getTpList() {
    //     return tpList;
    // } 

    public void startTpRequest(ServerPlayerEntity tpRequestSender, ServerPlayerEntity tpTargetPlayer) {
        PlayerData tpRequestSenderData = dataManager.getOrCreate(tpRequestSender);
        PlayerData tpTargetPlayerData = dataManager.getOrCreate(tpTargetPlayer);
        tpRequestSenderData.setTpTimer(60);
        tpRequestSenderData.setTpTarget(tpTargetPlayerData);
        tpList.add(tpRequestSenderData);
        tpTargetPlayerData.addTpAsker(tpRequestSenderData);

    }
    
    public void endTpRequest(ServerPlayerEntity tpRequestSender) {
        PlayerData data = dataManager.getOrCreate(tpRequestSender);
        data.setTpTimer(-1);
    }
}