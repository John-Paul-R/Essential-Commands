package com.fibermc.essentialcommands;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.LinkedList;
import java.util.ListIterator;

/**
 * TeleportRequestManager
 */
public class TeleportRequestManager {

    private static final int TPS = 20;
    private PlayerDataManager dataManager;
    private LinkedList<PlayerData> activeTpRequestList;
    private LinkedList<PlayerData> tpCooldownList;
    private LinkedList<PlayerData> tpDelayList;

    public TeleportRequestManager(PlayerDataManager dataManager) {
        this.dataManager = dataManager;
        activeTpRequestList = new LinkedList<>();
        tpCooldownList = new LinkedList<>();
        tpDelayList = new LinkedList<>();
        ServerTickEvents.END_SERVER_TICK.register(this::tick);
    }

    public void tick(MinecraftServer server) {
        ListIterator<PlayerData> iter;
        //decrement the tp timer for all players that have put in a tp request
        iter = activeTpRequestList.listIterator();
        while (iter.hasNext()) {
            PlayerData e = iter.next();
            e.tickTpTimer();
            if (e.getTpTimer() < 0) {
                PlayerData target = e.getTpTarget();
                if (target!=null) {
                    target.removeTpAsker(e);
                    e.setTpTarget(null);
                }
                iter.remove();
            }
        }

        iter = tpCooldownList.listIterator();
        while (iter.hasNext()) {
            PlayerData e = iter.next();
            e.tickTpCooldown();
            if (e.getTpCooldown() < 0) {
                iter.remove();
            }
        }

        iter = tpDelayList.listIterator();
        while (iter.hasNext()) {
            PlayerData e = iter.next();
            e.tickTpDelay();
            if (e.getTpDelay() < 0) {
                iter.remove();
            }
        }
    }

    // public List<PlayerData> getTpList() {
    //     return tpList;
    // }

    public void startTpRequest(ServerPlayerEntity requestSender, ServerPlayerEntity targetPlayer) {
        PlayerData requestSenderData = dataManager.getOrCreate(requestSender);
        PlayerData targetPlayerData = dataManager.getOrCreate(targetPlayer);

        final int TRD = Config.TELEPORT_REQUEST_DURATION;
        requestSenderData.setTpTimer(TRD*TPS);//sec * ticks per sec
        requestSenderData.setTpTarget(targetPlayerData);
        targetPlayerData.addTpAsker(requestSenderData);
        activeTpRequestList.add(requestSenderData);
    }

    public void startTpCooldown(ServerPlayerEntity player) {
        PlayerData pData = dataManager.getOrCreate(player);

        final double TC = Config.TELEPORT_COOLDOWN;
        pData.setTpCooldown((int)(TC*TPS));
        tpCooldownList.add(pData);
    }

    public void startTpDelay(ServerPlayerEntity player) {
        PlayerData pData = dataManager.getOrCreate(player);

        final double TD = Config.TELEPORT_DELAY;
        pData.setTpDelay((int)(TD*TPS));
        tpDelayList.add(pData);
    }


}