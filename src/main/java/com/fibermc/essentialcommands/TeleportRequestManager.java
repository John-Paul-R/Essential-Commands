package com.fibermc.essentialcommands;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.events.PlayerDamageCallback;
import com.fibermc.essentialcommands.types.MinecraftLocation;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;

/**
 * TeleportRequestManager
 */
public final class TeleportRequestManager {

    private static final int TPS = 20;
    private final LinkedList<TeleportRequest> activeTpRequestList;
    private final LinkedList<PlayerData> tpCooldownList;
    private final ConcurrentHashMap<UUID, QueuedTeleport> delayedQueuedTeleportMap;

    private static TeleportRequestManager instance;

    private TeleportRequestManager() {
        instance = this;
        activeTpRequestList = new LinkedList<>();
        tpCooldownList = new LinkedList<>();
        delayedQueuedTeleportMap = new ConcurrentHashMap<>();
    }

    public static TeleportRequestManager getInstance() {
        if (instance == null) {
            instance = new TeleportRequestManager();
        }
        return instance;
    }

    public static void init() {
        getInstance();
        PlayerDamageCallback.EVENT.register((ServerPlayerEntity playerEntity, DamageSource source) -> instance.onPlayerDamaged(playerEntity, source));
        PlayerDataManager.TICK_EVENT.register(((playerDataManager, server) -> instance.tick(server)));
    }

    public void tick(MinecraftServer server) {
        // Remove any requests that have ended since the last tick.
        activeTpRequestList.removeIf(TeleportRequest::isEnded);
        var lang = ECText.getInstance();
        // decrement the tp timer for all players that have put in a tp request
        for (TeleportRequest teleportRequest : activeTpRequestList) {
            PlayerData requesterPlayerData = ((ServerPlayerEntityAccess) teleportRequest.getSenderPlayer()).ec$getPlayerData();
            requesterPlayerData.tickTpTimer();
            if (requesterPlayerData.getTpTimer() < 0) {
                teleportRequest.end();
                // Teleport expiry message to sender
                teleportRequest.getSenderPlayerData().sendMessage(
                    "teleport.request.expired.sender",
                    teleportRequest.getTargetPlayer().getDisplayName()
                );
                // Teleport expiry message to receiver
                teleportRequest.getTargetPlayerData().sendMessage(
                    "teleport.request.expired.receiver",
                    teleportRequest.getSenderPlayer().getDisplayName()
                );
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

        var shouldInterruptTeleportOnMove = CONFIG.TELEPORT_INTERRUPT_ON_MOVE;
        var maxMoveBeforeInterrupt = CONFIG.TELEPORT_INTERRUPT_ON_MOVE_AMOUNT;
        Iterator<Map.Entry<UUID, QueuedTeleport>> tpQueueIter = delayedQueuedTeleportMap.entrySet().iterator();
        while (tpQueueIter.hasNext()) {
            Map.Entry<UUID, QueuedTeleport> entry = tpQueueIter.next();
            QueuedTeleport queuedTeleport = entry.getValue();
            queuedTeleport.tick(server);

            var playerData = queuedTeleport.getPlayerData();
            if (shouldInterruptTeleportOnMove
                && playerData.hasMovedThisTick()
                && playerData.getPlayer().getPos().distanceTo(queuedTeleport.initialPosition) > maxMoveBeforeInterrupt
                && !ECPerms.check(playerData.getPlayer().getCommandSource(), ECPerms.Registry.bypass_teleport_interrupt_on_move)
            ) {
                playerData.sendError("teleport.interruped.moved");
                tpQueueIter.remove();
            }

            if (queuedTeleport.getTicksRemaining() < 0) {
                tpQueueIter.remove();
                PlayerTeleporter.teleport(queuedTeleport);
            }
        }
    }

    public void onPlayerDamaged(ServerPlayerEntity playerEntity, DamageSource damageSource) {
        if (CONFIG.TELEPORT_INTERRUPT_ON_DAMAGED
            && !PlayerTeleporter.playerHasTpRulesBypass(playerEntity, ECPerms.Registry.bypass_teleport_interrupt_on_damaged)
        ) {
            try {
                var playerAccess = ((ServerPlayerEntityAccess) playerEntity);
                Objects.requireNonNull(playerAccess.ec$endQueuedTeleport());

                delayedQueuedTeleportMap.remove(playerEntity.getUuid());
                playerAccess.ec$getPlayerData().sendError("teleport.interrupted.damage");
            } catch (NullPointerException ignored) {}
        }
    }

    public void startTpRequest(ServerPlayerEntity requestSender, ServerPlayerEntity targetPlayer, TeleportRequest.Type requestType) {
        PlayerData requestSenderData = ((ServerPlayerEntityAccess) requestSender).ec$getPlayerData();
        PlayerData targetPlayerData = ((ServerPlayerEntityAccess) targetPlayer).ec$getPlayerData();

        final int teleportRequestDurationTicks = CONFIG.TELEPORT_REQUEST_DURATION * TPS; //sec * ticks per sec
        requestSenderData.setTpTimer(teleportRequestDurationTicks);
        TeleportRequest teleportRequest = new TeleportRequest(requestSender, targetPlayer, requestType);
        requestSenderData.setSentTeleportRequest(teleportRequest);
        targetPlayerData.addIncomingTeleportRequest(teleportRequest);
        activeTpRequestList.add(teleportRequest);
    }

    public void startTpCooldown(ServerPlayerEntity player) {
        PlayerData pData = ((ServerPlayerEntityAccess) player).ec$getPlayerData();

        final int teleportCooldownTicks = (int) (CONFIG.TELEPORT_COOLDOWN * TPS);
        pData.setTpCooldown(teleportCooldownTicks);
        tpCooldownList.add(pData);
    }

    public void queueTeleport(ServerPlayerEntity player, MinecraftLocation dest, MutableText destName) {
        queueTeleport(new QueuedLocationTeleport(((ServerPlayerEntityAccess) player).ec$getPlayerData(), dest, destName));
    }

    public void queueTeleport(QueuedTeleport queuedTeleport) {
        QueuedTeleport prevValue = delayedQueuedTeleportMap.put(
            queuedTeleport.getPlayerData().getPlayer().getUuid(),
            queuedTeleport
        );
        if (prevValue != null) {
            var styleUpdater = TextFormatType.Accent.nonOverwritingStyleUpdater();
            prevValue.getPlayerData().sendMessage(
                "teleport.request.canceled_by_new",
                prevValue.getDestName().styled(styleUpdater),
                queuedTeleport.getDestName().styled(styleUpdater)
            );
        }

    }
}
