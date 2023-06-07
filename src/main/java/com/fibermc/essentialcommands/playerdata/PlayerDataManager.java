package com.fibermc.essentialcommands.playerdata;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fibermc.essentialcommands.ManagerLocator;
import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.events.*;
import com.fibermc.essentialcommands.types.MinecraftLocation;
import com.fibermc.essentialcommands.types.RespawnCondition;
import eu.pb4.placeholders.PlaceholderAPI;
import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

import dev.jpcode.eccore.config.expression.ExpressionEvaluationContext;

import dev.jpcode.eccore.util.TextUtil;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;

public class PlayerDataManager {

    private final ConcurrentHashMap<UUID, PlayerData> dataMap;
    private final List<PlayerData> changedNicknames;
    private final List<String> changedTeams;
    private final List<Runnable> nextTickTasks;
    private static PlayerDataManager instance;

    public PlayerDataManager() {
        instance = this;
        this.changedNicknames = new LinkedList<>();
        this.changedTeams = new LinkedList<>();
        this.nextTickTasks = new LinkedList<>();
        this.dataMap = new ConcurrentHashMap<>();
    }

    public static void init() {
        PlayerConnectCallback.EVENT.register(PlayerDataManager::initializePlayerDataForConnect);
        PlayerLeaveCallback.EVENT.register(PlayerDataManager::handleUnloadPlayerDataForLeave);
        PlayerDeathCallback.EVENT.register(PlayerDataManager::handleSetPreviousLocationForDeath);
        PlayerRespawnCallback.EVENT.register(PlayerDataManager::handlePlayerDataRespawnSync);
        PlayerRespawnCallback.EVENT.register(PlayerDataManager::handleRespawnAtEcSpawn);
        ServerTickEvents.END_SERVER_TICK.register((MinecraftServer server) -> PlayerDataManager.getInstance().tick(server));
        ServerPlayConnectionEvents.JOIN.register(PlayerDataManager::handleSendMotdForGameJoin);
    }

    public static final Event<PlayerDataManagerTickCallback> TICK_EVENT = EventFactory.createArrayBacked(
        PlayerDataManagerTickCallback.class,
        (listeners) -> (playerDataManager, server) -> {
            for (PlayerDataManagerTickCallback event : listeners) {
                event.onTick(playerDataManager, server);
                server.getOverworld().updateSleepingPlayers();
            }
        });

    private static void handleSendMotdForGameJoin(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server) {
        if (CONFIG.ENABLE_MOTD) {
            var player = handler.getPlayer();
            var message = PlaceholderAPI.parseText(
                TextUtil.literal(CONFIG.MOTD),
                player
            );
            player.getCommandSource().sendFeedback(message, false);
        }
    }

    public static boolean exists() {
        return instance != null;
    }
    public static PlayerDataManager getInstance() {
        return instance != null ? instance : new PlayerDataManager();
    }

    public void markNicknameDirty(PlayerData playerData) {
        changedNicknames.add(playerData);
    }

    public void markNicknameDirty(String playerName) {
        changedTeams.add(playerName);
    }

    public void tick(MinecraftServer server) {
        if (CONFIG.NICKNAMES_IN_PLAYER_LIST && server.getTicks() % (20 * 5) == 0) {
            if (this.changedNicknames.size() + this.changedTeams.size() > 0) {
                PlayerManager serverPlayerManager = server.getPlayerManager();

                Set<ServerPlayerEntity> allChangedNicknamePlayers = Stream.concat(
                    changedNicknames.stream().map(PlayerData::getPlayer),
                    changedTeams.stream().map(serverPlayerManager::getPlayer)
                ).filter(Objects::nonNull).collect(Collectors.toSet());

                server.getPlayerManager().sendToAll(new PlayerListS2CPacket(
                    PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME,
                    allChangedNicknamePlayers
                ));

                changedNicknames.forEach(PlayerData::save);

                this.changedNicknames.clear();
                this.changedTeams.clear();
            }
        }

        if (!nextTickTasks.isEmpty()) {
            Iterator<Runnable> tasks = nextTickTasks.listIterator();
            while (tasks.hasNext()) {
                tasks.next().run();
                tasks.remove();
            }
        }

        TICK_EVENT.invoker().onTick(this, server);

        getAllPlayerData().forEach(PlayerData::onTickEnd);
    }

    public void scheduleTask(Runnable task) {
        this.nextTickTasks.add(task);
    }

    // EVENTS
    private static void initializePlayerDataForConnect(ClientConnection connection, ServerPlayerEntity player) {
        var playerAccess = ((ServerPlayerEntityAccess) player);
        PlayerData playerData = getInstance().loadPlayerData(player);
        playerAccess.ec$setPlayerData(playerData);

        playerAccess.ec$getProfile();
        playerAccess.ec$getEcText();
    }

    private static void handleUnloadPlayerDataForLeave(ServerPlayerEntity player) {
        // Auto-saving should be handled by WorldSaveHandlerMixin. (PlayerData saves when MC server saves players)
        getInstance().unloadPlayerData(player);
    }

    private static void handlePlayerDataRespawnSync(ServerPlayerEntity oldPlayerEntity, ServerPlayerEntity newPlayerEntity) {
        var oldPlayerAccess = ((ServerPlayerEntityAccess) oldPlayerEntity);
        var newPlayerAccess = ((ServerPlayerEntityAccess) newPlayerEntity);

        PlayerData playerData = oldPlayerAccess.ec$getPlayerData();
        playerData.updatePlayerEntity(newPlayerEntity);
        newPlayerAccess.ec$setPlayerData(playerData);

        PlayerProfile profile = oldPlayerAccess.ec$getProfile();
        profile.updatePlayerEntity(newPlayerEntity);
        newPlayerAccess.ec$setProfile(profile);
    }

    private static void handleRespawnAtEcSpawn(ServerPlayerEntity oldPlayerEntity, ServerPlayerEntity newPlayerEntity) {
        var worldMgr = ManagerLocator.getInstance().getWorldDataManager();
        var spawnLocOpt = worldMgr.getSpawn();
        if (spawnLocOpt.isEmpty()) {
            return;
        }

        var spawnLoc = spawnLocOpt.get();

        ExpressionEvaluationContext<RespawnCondition> ctx = new ExpressionEvaluationContext<>() {
            private boolean isSameWorld() {
                return oldPlayerEntity.getServerWorld().getRegistryKey() == spawnLoc.dim();
            }

            private boolean hasNoBed() {
                var vanillaPlayerSpawnPoint = newPlayerEntity.getSpawnPointPosition();
                return vanillaPlayerSpawnPoint == null;
            }

            @Override
            public boolean matches(RespawnCondition condition) {
                return switch (condition) {
                    case Never -> false;
                    case Always -> true;
                    case SameWorld -> isSameWorld();
                    case NoBed -> hasNoBed();
                };
            }
        };

        if (CONFIG.RESPAWN_AT_EC_SPAWN.matches(ctx)) {
            // respawn at spawn loc
            // This event handler executes just before the player is truly respawned, so we can just
            // modify the entity's location to achieve this.
            newPlayerEntity.setPosition(spawnLoc.pos());
            newPlayerEntity.setWorld(newPlayerEntity.getServer().getWorld(spawnLoc.dim()));
        }
    }

    private static void handleSetPreviousLocationForDeath(ServerPlayerEntity playerEntity, DamageSource damageSource) {
        PlayerData pData = ((ServerPlayerEntityAccess) playerEntity).ec$getPlayerData();
        if (CONFIG.ALLOW_BACK_ON_DEATH) {
            pData.setPreviousLocation(new MinecraftLocation(pData.getPlayer()));
        }
    }

    // SET / ADD
    private PlayerData loadPlayerData(ServerPlayerEntity player) {
        PlayerData playerData = ((ServerPlayerEntityAccess) player).ec$getPlayerData();
        dataMap.put(player.getUuid(), playerData);
        return playerData;
    }

    PlayerData getPlayerDataFromUUID(UUID playerID) {
        return dataMap.get(playerID);
    }

    // SAVE / LOAD
    private void unloadPlayerData(ServerPlayerEntity player) {
        this.dataMap.remove(player.getUuid());
    }

    public Collection<PlayerData> getAllPlayerData() {
        return dataMap.values();
    }

    @Nullable
    public PlayerData getByUuid(UUID uuid) {
        return dataMap.get(uuid);
    }

    /**
     * Case insentitive
     */
    public List<PlayerData> getPlayerDataMatchingNickname(String nickname) {
        return dataMap.values().stream()
            .filter(playerData -> playerData
                .getNickname()
                .map(nick -> nick.getString().equalsIgnoreCase(nickname))
                .orElse(false))
            .collect(Collectors.toList());
    }
}
