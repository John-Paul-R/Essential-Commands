package com.fibermc.essentialcommands.teleportation;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.fibermc.essentialcommands.ECPerms;
import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.events.PlayerDamageCallback;
import com.fibermc.essentialcommands.playerdata.PlayerData;
import com.fibermc.essentialcommands.playerdata.PlayerDataManager;
import com.fibermc.essentialcommands.text.TextFormatType;
import com.fibermc.essentialcommands.types.MinecraftLocation;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;

import dev.jpcode.eccore.util.TimeUtil;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;

public final class TeleportManager {
    private final List<TeleportRequest> activeTeleportRequests;
    private final List<PlayerData> playersOnTeleportCooldown;
    private final Map<UUID, QueuedTeleport> queuedTeleportMap;

    private static TeleportManager instance;

    private TeleportManager() {
        instance = this;
        activeTeleportRequests = new LinkedList<>();
        playersOnTeleportCooldown = new LinkedList<>();
        queuedTeleportMap = new ConcurrentHashMap<>();
    }

    public static TeleportManager getInstance() {
        if (instance == null) {
            instance = new TeleportManager();
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
        activeTeleportRequests.removeIf(TeleportRequest::isEnded);
        // decrement the tp timer for all players that have put in a tp request
        for (TeleportRequest teleportRequest : activeTeleportRequests) {
            teleportRequest.incrementAgeTicks();

            // Handle teleport expiry
            if (teleportRequest.getAgeTicks() > CONFIG.TELEPORT_REQUEST_DURATION_TICKS) {
                teleportRequest.end();
                teleportRequest.getSenderPlayerData().sendMessage(
                    "teleport.request.expired.sender",
                    teleportRequest.getTargetPlayer().getDisplayName()
                );
                teleportRequest.getTargetPlayerData().sendMessage(
                    "teleport.request.expired.receiver",
                    teleportRequest.getSenderPlayer().getDisplayName()
                );
            }
        }

        ListIterator<PlayerData> tpCooldownIterator = playersOnTeleportCooldown.listIterator();
        while (tpCooldownIterator.hasNext()) {
            PlayerData playerData = tpCooldownIterator.next();
            playerData.tickTpCooldown();
            if (playerData.getTpCooldown() < 0) {
                tpCooldownIterator.remove();
            }
        }

        var shouldInterruptTeleportOnMove = CONFIG.TELEPORT_INTERRUPT_ON_MOVE;
        var maxMoveBeforeInterrupt = CONFIG.TELEPORT_INTERRUPT_ON_MOVE_AMOUNT;
        Iterator<Map.Entry<UUID, QueuedTeleport>> tpQueueIter = queuedTeleportMap.entrySet().iterator();
        while (tpQueueIter.hasNext()) {
            Map.Entry<UUID, QueuedTeleport> entry = tpQueueIter.next();
            QueuedTeleport queuedTeleport = entry.getValue();
            queuedTeleport.tick(server);

            var playerData = queuedTeleport.getPlayerData();
            if (shouldInterruptTeleportOnMove
                && playerData.hasMovedThisTick()
                && playerData.getPlayer().getPos().distanceTo(queuedTeleport.initialPosition) > maxMoveBeforeInterrupt
                && !PlayerTeleporter.playerHasTpRulesBypass(playerData.getPlayer(), ECPerms.Registry.bypass_teleport_interrupt_on_move)
            ) {
                playerData.sendError("teleport.interruped.moved");
                tpQueueIter.remove();
                continue;
            }

            if (queuedTeleport.getTicksRemaining() < 0) {
                tpQueueIter.remove();
                PlayerTeleporter.teleport(queuedTeleport);
            }
        }
    }

    public void onPlayerDamaged(ServerPlayerEntity playerEntity, DamageSource damageSource) {
        if (!CONFIG.TELEPORT_INTERRUPT_ON_DAMAGED) {
            return;
        }
        var playerAccess = ((ServerPlayerEntityAccess) playerEntity);
        if (playerAccess.ec$getQueuedTeleport() != null
            && !PlayerTeleporter.playerHasTpRulesBypass(playerEntity, ECPerms.Registry.bypass_teleport_interrupt_on_damaged)
        ) {
            playerAccess.ec$endQueuedTeleport();
            queuedTeleportMap.remove(playerEntity.getUuid());
            playerAccess.ec$getPlayerData().sendError("teleport.interrupted.damage");
        }
    }

    public void startTpRequest(
        ServerPlayerEntity requestSender,
        ServerPlayerEntity targetPlayer,
        TeleportRequest.Type requestType
    )
    {
        var senderPlayerData = PlayerData.access(requestSender);
        var targetPlayerData = PlayerData.access(targetPlayer);

        var teleportRequest = new TeleportRequest(requestSender, targetPlayer, requestType);
        senderPlayerData.addSentTeleportRequest(teleportRequest);
        targetPlayerData.addIncomingTeleportRequest(teleportRequest);
        activeTeleportRequests.add(teleportRequest);
    }

    public void startTpCooldown(ServerPlayerEntity player) {
        final int teleportCooldownTicks = (int) (CONFIG.TELEPORT_COOLDOWN * TimeUtil.TPS);
        var playerData = PlayerData.access(player);

        playerData.setTpCooldown(teleportCooldownTicks);
        playersOnTeleportCooldown.add(playerData);
    }

    // Generally, you should use PlayerTeleporter.requestTeleport instead of calling the queueTeleport methods directly.
    void queueTeleport(ServerPlayerEntity player, MinecraftLocation dest, MutableText destName) {
        queueTeleport(new QueuedLocationTeleport(PlayerData.access(player), dest, destName));
    }

    void queueTeleport(QueuedTeleport queuedTeleport) {
        var playerData = queuedTeleport.getPlayerData();
        var playerAccess = ((ServerPlayerEntityAccess) playerData.getPlayer());

        QueuedTeleport prevValue = queuedTeleportMap.put(
            queuedTeleport.getPlayerData().getPlayer().getUuid(),
            queuedTeleport
        );
        if (prevValue != null) {
            var profile = playerAccess.ec$getProfile();
            var styleUpdater = profile.nonOverwritingColorUpdater(TextFormatType.Accent);
            prevValue.getPlayerData().sendMessage(
                "teleport.request.canceled_by_new",
                prevValue.getDestName().styled(styleUpdater),
                queuedTeleport.getDestName().styled(styleUpdater)
            );
        }

        playerAccess.ec$setQueuedTeleport(queuedTeleport);
        playerData.sendMessage(
            "teleport.queued",
            queuedTeleport.getDestName().setStyle(playerAccess.ec$getProfile().getStyle(TextFormatType.Accent)),
            playerAccess.ec$getEcText().accent(String.format("%.1f", TimeUtil.ticksToSeconds(CONFIG.TELEPORT_DELAY_TICKS)))
        );
    }
}
