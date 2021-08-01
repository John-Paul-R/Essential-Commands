package com.fibermc.essentialcommands;

import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.config.Config;
import com.fibermc.essentialcommands.events.PlayerDamageCallback;
import com.fibermc.essentialcommands.types.MinecraftLocation;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Util;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * TeleportRequestManager
 */
public class TeleportRequestManager {

    private static final int TPS = 20;
    private final PlayerDataManager dataManager;
    private final LinkedList<TeleportRequest> activeTpRequestList;
    private final LinkedList<PlayerData> tpCooldownList;
    private final ConcurrentHashMap<UUID, QueuedTeleport> delayedQueuedTeleportMap;

    private static TeleportRequestManager INSTANCE;

    public TeleportRequestManager(PlayerDataManager dataManager) {
        INSTANCE = this;
        this.dataManager = dataManager;
        activeTpRequestList = new LinkedList<TeleportRequest>();
        tpCooldownList = new LinkedList<>();
        delayedQueuedTeleportMap = new ConcurrentHashMap<>();
    }

    public static TeleportRequestManager getInstance() {
        return INSTANCE;
    }

    public static void init() {
        PlayerDamageCallback.EVENT.register((ServerPlayerEntity playerEntity, DamageSource source) -> TeleportRequestManager.INSTANCE.onPlayerDamaged(playerEntity, source));
        ServerTickEvents.END_SERVER_TICK.register((MinecraftServer server) -> TeleportRequestManager.INSTANCE.tick(server));
    }

    public void endTpRequest(TeleportRequest teleportRequest) {
        endTpRequestFinal(teleportRequest);
        this.activeTpRequestList.remove(teleportRequest);
    }
    private void endTpRequestFinal(TeleportRequest teleportRequest) {
        PlayerData target = teleportRequest.getTargetPlayerData();
        if (target != null) {
            target.removeIncomingTeleportRequest(teleportRequest.getSenderPlayer().getUuid());
            teleportRequest.getSenderPlayerData().setSentTeleportRequest(null);
        }
    }

    public void tick(MinecraftServer server) {
        ListIterator<TeleportRequest> tpRequestIterator = activeTpRequestList.listIterator();
        //decrement the tp timer for all players that have put in a tp request
        while (tpRequestIterator.hasNext()) {
            TeleportRequest teleportRequest = tpRequestIterator.next();
            PlayerData requesterPlayerData = ((ServerPlayerEntityAccess) teleportRequest.getSenderPlayer()).getEcPlayerData();
            requesterPlayerData.tickTpTimer();
            if (requesterPlayerData.getTpTimer() < 0) {
                endTpRequestFinal(teleportRequest);
                tpRequestIterator.remove();
            }
        }

        ListIterator<PlayerData> toCooldownIterator = tpCooldownList.listIterator();
        while (toCooldownIterator.hasNext()) {
            PlayerData e = toCooldownIterator.next();
            e.tickTpCooldown();
            if (e.getTpCooldown() < 0) {
                toCooldownIterator.remove();
            }
        }

        Iterator<Map.Entry<UUID, QueuedTeleport>> tpQueueIter = delayedQueuedTeleportMap.entrySet().iterator();
        while (tpQueueIter.hasNext()) {
            Map.Entry<UUID, QueuedTeleport> entry = tpQueueIter.next();
            QueuedTeleport queuedTeleport = entry.getValue();
            queuedTeleport.tick(server);
            if (queuedTeleport.getTicksRemaining() < 0) {
                tpQueueIter.remove();
                PlayerTeleporter.teleport(queuedTeleport);
            }
        }
    }

    public void onPlayerDamaged(ServerPlayerEntity playerEntity, DamageSource damageSource) {
        if (Config.TELEPORT_INTERRUPT_ON_DAMAGED && !PlayerTeleporter.playerHasTpRulesBypass(playerEntity, ECPerms.Registry.bypass_teleport_interrupt_on_damaged)) {
            try {
                Objects.requireNonNull( ((ServerPlayerEntityAccess)playerEntity).endEcQueuedTeleport());

                delayedQueuedTeleportMap.remove(playerEntity.getUuid());
                playerEntity.sendSystemMessage(
                    new LiteralText("Teleport interrupted. Reason: Damage Taken").setStyle(Config.FORMATTING_ERROR),
                    Util.NIL_UUID
                );
            } catch (NullPointerException ignored) {}
        }
    }

    public void startTpRequest(ServerPlayerEntity requestSender, ServerPlayerEntity targetPlayer, TeleportRequest.Type requestType) {
        PlayerData requestSenderData = ((ServerPlayerEntityAccess)requestSender).getEcPlayerData();
        PlayerData targetPlayerData = ((ServerPlayerEntityAccess)targetPlayer).getEcPlayerData();

        final int TRD = Config.TELEPORT_REQUEST_DURATION * TPS;//sec * ticks per sec
        requestSenderData.setTpTimer(TRD);
        TeleportRequest teleportRequest = new TeleportRequest(requestSender, targetPlayer, requestType);
        requestSenderData.setSentTeleportRequest(teleportRequest);
        targetPlayerData.addIncomingTeleportRequest(teleportRequest);
        activeTpRequestList.add(teleportRequest);
    }

    public void startTpCooldown(ServerPlayerEntity player) {
        PlayerData pData = ((ServerPlayerEntityAccess)player).getEcPlayerData();

        final int TC = (int)(Config.TELEPORT_COOLDOWN * TPS);
        pData.setTpCooldown(TC);
        tpCooldownList.add(pData);
    }

    public void queueTeleport(ServerPlayerEntity player, MinecraftLocation dest, MutableText destName) {
        queueTeleport(new QueuedLocationTeleport(((ServerPlayerEntityAccess)player).getEcPlayerData(), dest, destName));
    }


    public void queueTeleport(QueuedTeleport queuedTeleport) {
        QueuedTeleport prevValue = delayedQueuedTeleportMap.put(
            queuedTeleport.getPlayerData().getPlayer().getUuid(),
            queuedTeleport
        );
        if (Objects.nonNull(prevValue)) {
            prevValue.getPlayerData().getPlayer().sendSystemMessage(
                new LiteralText("Teleport request canceled. Reason: New teleport started!")
                    .setStyle(Config.FORMATTING_DEFAULT),
                Util.NIL_UUID
            );
        }

    }
}