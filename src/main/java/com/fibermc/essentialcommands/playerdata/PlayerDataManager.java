package com.fibermc.essentialcommands.playerdata;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fibermc.essentialcommands.ManagerLocator;
import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.commands.MotdCommand;
import com.fibermc.essentialcommands.events.PlayerConnectCallback;
import com.fibermc.essentialcommands.events.PlayerDataManagerTickCallback;
import com.fibermc.essentialcommands.events.PlayerDeathCallback;
import com.fibermc.essentialcommands.events.PlayerLeaveCallback;
import com.fibermc.essentialcommands.types.MinecraftLocation;
import com.fibermc.essentialcommands.types.RespawnCondition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.damagesource.DamageSource;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

import dev.jpcode.eccore.config.expression.ExpressionEvaluationContext;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;

public class PlayerDataManager {

    private final ConcurrentHashMap<UUID, PlayerData> dataMap;
    private final ConcurrentHashMap<String, List<PlayerData>> dataByNicknameMap;
    private final List<PlayerData> changedNicknames;
    private final List<String> changedTeams;
    private final List<ServerTask> nextTickTasks;
    private static PlayerDataManager instance;

    public PlayerDataManager() {
        instance = this;
        this.changedNicknames = new ArrayList<>();
        this.changedTeams = new ArrayList<>();
        this.nextTickTasks = new ArrayList<>();
        this.dataMap = new ConcurrentHashMap<>();
        this.dataByNicknameMap = new ConcurrentHashMap<>();
    }

    public static void init() {
        PlayerConnectCallback.EVENT.register(
            PlayerDataManager::initializePlayerDataForConnect
        );
        PlayerLeaveCallback.EVENT.register(
            PlayerDataManager::handleUnloadPlayerDataForLeave
        );
        PlayerDeathCallback.EVENT.register(
            PlayerDataManager::handleSetPreviousLocationForDeath
        );
        ServerTickEvents.END_SERVER_TICK.register((MinecraftServer server) ->
            PlayerDataManager.getInstance().tick(server)
        );
        ServerPlayConnectionEvents.JOIN.register(
            PlayerDataManager::handleSendMotdForGameJoin
        );
    }

    public static final Event<PlayerDataManagerTickCallback> TICK_EVENT =
        EventFactory.createArrayBacked(
            PlayerDataManagerTickCallback.class,
            listeners ->
                (playerDataManager, server) -> {
                    for (PlayerDataManagerTickCallback event : listeners) {
                        event.onTick(playerDataManager, server);
                        server.overworld().updateSleepingPlayerList();
                    }
                }
        );

    private static void handleSendMotdForGameJoin(
        ServerGamePacketListenerImpl handler,
        PacketSender sender,
        MinecraftServer server
    ) {
        if (CONFIG.ENABLE_MOTD) {
            var player = handler.getPlayer();
            MotdCommand.exec(player);
        }
    }

    public static boolean exists() {
        return instance != null;
    }

    public static PlayerDataManager getInstance() {
        return instance != null ? instance : new PlayerDataManager();
    }

    public void markDisplayNameDirty(PlayerData playerData) {
        changedNicknames.add(playerData);
    }

    public void markNicknameDirty(PlayerData playerData, @Nullable String oldNormalizedNickname) {
        markDisplayNameDirty(playerData);
        removeFromNicknameMap(playerData, oldNormalizedNickname);
        addToNicknameMap(playerData);
    }

    public void markNicknameDirty(String playerName) {
        changedTeams.add(playerName);
    }

    public void queueNicknameUpdatesForAllPlayers() {
        scheduleTask("nickname-update", server -> {
            server
                .getPlayerList()
                .broadcastAll(
                    new ClientboundPlayerInfoUpdatePacket(
                        EnumSet.of(
                            ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME
                        ),
                        this.getAllPlayerData()
                            .stream()
                            .filter(pd -> pd.getNickname().isPresent())
                            .map(PlayerData::getPlayer)
                            .toList()
                    )
                );
        });
    }

    public void tick(MinecraftServer server) {
        if (
            CONFIG.NICKNAMES_IN_PLAYER_LIST && server.getTickCount() % (20 * 5) == 0
        ) {
            if (this.changedNicknames.size() + this.changedTeams.size() > 0) {
                PlayerList serverPlayerManager = server.getPlayerList();

                Set<ServerPlayer> allChangedNicknamePlayers =
                    Stream.concat(
                        changedNicknames.stream().map(PlayerData::getPlayer),
                        changedTeams
                            .stream()
                            .map(serverPlayerManager::getPlayerByName)
                    )
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet());

                server
                    .getPlayerList()
                    .broadcastAll(
                        new ClientboundPlayerInfoUpdatePacket(
                            EnumSet.of(
                                ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME
                            ),
                            allChangedNicknamePlayers
                        )
                    );

                changedNicknames.forEach(playerData ->
                    playerData.save()
                );

                this.changedNicknames.clear();
                this.changedTeams.clear();
            }
        }

        {
            List<ServerTask> tasksSnapshot = null;
            synchronized (nextTickTasks) {
                if (!nextTickTasks.isEmpty()) {
                    tasksSnapshot = new ArrayList<>(nextTickTasks);
                    nextTickTasks.clear();
                }
            }

            if (tasksSnapshot != null) {
                for (ServerTask nextTickTask : tasksSnapshot) {
                    nextTickTask.task().accept(server);
                }
            }
        }

        TICK_EVENT.invoker().onTick(this, server);

        getAllPlayerData().forEach(PlayerData::onTickEnd);
    }

    public void scheduleTask(Runnable task) {
        this.nextTickTasks.add(ServerTask.of(null, task));
    }

    public void scheduleTask(Consumer<MinecraftServer> task) {
        this.nextTickTasks.add(ServerTask.of(null, task));
    }

    public void scheduleTask(
        @NotNull String id,
        Consumer<MinecraftServer> task
    ) {
        // When id provided, avoid duplicates on id
        if (
            nextTickTasks
                .stream()
                .anyMatch(existingTask -> id.equals(existingTask.id()))
        ) {
            return;
        }
        this.nextTickTasks.add(ServerTask.of(id, task));
    }

    // EVENTS
    private static void initializePlayerDataForConnect(
        Connection connection,
        ServerPlayer player
    ) {
        var playerAccess = ((ServerPlayerEntityAccess) player);
        PlayerData playerData = getInstance().loadPlayerData(player);
        playerAccess.ec$setPlayerData(playerData);

        playerAccess.ec$getProfile();
        playerAccess.ec$getEcText();
    }

    private static void handleUnloadPlayerDataForLeave(
        ServerPlayer player
    ) {
        // Auto-saving should be handled by WorldSaveHandlerMixin. (PlayerData saves when MC server saves players)
        getInstance().unloadPlayerData(player);
    }

    public static void handlePlayerDataRespawnSync(
        ServerPlayer oldPlayerEntity,
        ServerPlayer newPlayerEntity
    ) {
        var oldPlayerAccess = ((ServerPlayerEntityAccess) oldPlayerEntity);
        var newPlayerAccess = ((ServerPlayerEntityAccess) newPlayerEntity);

        PlayerData playerData = oldPlayerAccess.ec$getPlayerData();
        playerData.updatePlayerEntity(newPlayerEntity);
        newPlayerAccess.ec$setPlayerData(playerData);

        PlayerProfile profile = oldPlayerAccess.ec$getProfile();
        profile.updatePlayerEntity(newPlayerEntity);
        newPlayerAccess.ec$setProfile(profile);
    }

    /**
     * @param oldPlayerEntity null if first spawn
     */
    public static void handleRespawnAtEcSpawn(
        @Nullable ServerPlayer oldPlayerEntity,
        Consumer<MinecraftLocation> onOverwriteSpawn
    ) {
        var worldMgr = ManagerLocator.getInstance().getWorldDataManager();
        var spawnLocOpt = worldMgr.getSpawn();
        if (spawnLocOpt.isEmpty()) {
            return;
        }

        var spawnLoc = spawnLocOpt.get();

        ExpressionEvaluationContext<RespawnCondition> ctx =
            new ExpressionEvaluationContext<>() {
                private boolean isSameWorld() {
                    return (
                        oldPlayerEntity == null
                        || oldPlayerEntity.level().dimension() == spawnLoc.dim()
                    );
                }

                private boolean hasNoBed() {
                    return (
                        oldPlayerEntity == null
                        // This is not perfect, but 'respawn' is horribly
                        // complex to navigate now
                        || oldPlayerEntity.getRespawnConfig() == null
                    );
                }

                @Override
                public boolean matches(RespawnCondition condition) {
                    return switch (condition) {
                        case Never -> false;
                        case Always -> true;
                        case SameWorld -> isSameWorld();
                        case NoBed -> hasNoBed();
                        case FirstJoin -> oldPlayerEntity == null;
                    };
                }
            };

        if (CONFIG.RESPAWN_AT_EC_SPAWN.matches(ctx)) {
            // respawn at spawn loc
            // This event handler executes just before the player is truly respawned, so we can just
            // modify the entity's location to achieve this.
            onOverwriteSpawn.accept(spawnLoc);
        }
    }

    private static void handleSetPreviousLocationForDeath(
        ServerPlayer playerEntity,
        DamageSource damageSource
    ) {
        PlayerData pData =
            ((ServerPlayerEntityAccess) playerEntity).ec$getPlayerData();
        if (CONFIG.ALLOW_BACK_ON_DEATH) {
            pData.setPreviousLocation(new MinecraftLocation(pData.getPlayer()));
        }
    }

    // SET / ADD
    private PlayerData loadPlayerData(ServerPlayer player) {
        PlayerData playerData =
            ((ServerPlayerEntityAccess) player).ec$getPlayerData();
        dataMap.put(player.getUUID(), playerData);
        addToNicknameMap(playerData);
        return playerData;
    }

    PlayerData getPlayerDataFromUUID(UUID playerID) {
        return dataMap.get(playerID);
    }

    // SAVE / LOAD
    private void unloadPlayerData(ServerPlayer player) {
        PlayerData playerData = this.dataMap.remove(player.getUUID());
        if (playerData != null) {
            removeFromNicknameMap(playerData, playerData.getNormalizedNickname());
        }
    }

    public Collection<PlayerData> getAllPlayerData() {
        return dataMap.values();
    }

    @Nullable
    public PlayerData getByUuid(UUID uuid) {
        return dataMap.get(uuid);
    }

    private static String nicknameKey(String nickname) {
        return PlayerData.normalizeNickname(nickname);
    }

    private void addToNicknameMap(PlayerData playerData) {
        String key = playerData.getNormalizedNickname();
        if (key != null && !key.isBlank()) {
            dataByNicknameMap.compute(key, (k, list) -> {
                if (list == null) list = new ArrayList<>();
                list.add(playerData);
                return list;
            });
        }
    }

    // Key is passed explicitly because the caller captures it before mutation.
    private void removeFromNicknameMap(PlayerData playerData, @Nullable String key) {
        if (key != null) {
            dataByNicknameMap.computeIfPresent(key, (k, list) -> {
                list.remove(playerData);
                return list.isEmpty() ? null : list;
            });
        }
    }

    // Case-insensitive, whitespace-insensitive lookup.
    public List<PlayerData> getByNickname(String nickname) {
        List<PlayerData> result = dataByNicknameMap.get(nicknameKey(nickname));
        return result != null ? List.copyOf(result) : List.of();
    }

    public List<PlayerData> getPlayerDataMatchingNickname(String nickname) {
        return getByNickname(nickname);
    }
}
