package com.fibermc.essentialcommands;

import com.fibermc.essentialcommands.types.MinecraftLocation;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

import java.util.*;

/**
 * TeleportRequestManager
 */
public class TeleportRequestManager {

    private static final int TPS = 20;
    private PlayerDataManager dataManager;
    private LinkedList<PlayerData> activeTpRequestList;
    private LinkedList<PlayerData> tpCooldownList;
    private HashMap<UUID, QueuedTeleport> delayedTeleportQueue;

    private static TeleportRequestManager INSTANCE;

    public TeleportRequestManager(PlayerDataManager dataManager) {
        INSTANCE = this;
        this.dataManager = dataManager;
        activeTpRequestList = new LinkedList<>();
        tpCooldownList = new LinkedList<>();
        delayedTeleportQueue = new HashMap<>();
    }

    public static TeleportRequestManager getInstance() {
        return INSTANCE;
    }

    public static void init() {
        ServerTickEvents.END_SERVER_TICK.register((MinecraftServer server) -> TeleportRequestManager.INSTANCE.tick(server));
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

        Iterator<Map.Entry<UUID, QueuedTeleport>> tpQueueIter = delayedTeleportQueue.entrySet().iterator();
        while (tpQueueIter.hasNext()) {
            Map.Entry<UUID, QueuedTeleport> entry = tpQueueIter.next();
            QueuedTeleport tp = entry.getValue();
            tp.tick(server);
            if (tp.getTicksRemaining() < 0) {
                tpQueueIter.remove();
                PlayerTeleporter.teleport(tp.getPlayerData(), tp.getDest());
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

    public void queueTeleport(ServerPlayerEntity player, MinecraftLocation dest, String destName) {
        final double TD = Config.TELEPORT_DELAY;
        PlayerData pData = dataManager.getOrCreate(player);

        QueuedTeleport prevValue = delayedTeleportQueue.put(player.getUuid(), new QueuedTeleport(pData, dest, destName, (int)(TD*TPS)));
        if (Objects.nonNull(prevValue)) {
            prevValue.getPlayerData().getPlayer().sendSystemMessage(
                new LiteralText("Teleport request canceled. Reason: New teleport started!")
                    .formatted(Config.FORMATTING_DEFAULT),
                new UUID(0,0)
            );
        }
    }


}