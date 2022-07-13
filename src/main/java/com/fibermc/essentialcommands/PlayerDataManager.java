package com.fibermc.essentialcommands;

import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.events.*;
import com.fibermc.essentialcommands.types.MinecraftLocation;
import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.TextParserUtils;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;

public class PlayerDataManager {

    private final ConcurrentHashMap<UUID, PlayerData> dataMap;
    private final List<PlayerData> changedNicknames;
    private final List<String> changedTeams;
    private final List<Runnable> nextTickTasks;
    private static PlayerDataManager INSTANCE;

    public PlayerDataManager() {
        INSTANCE = this;
        this.changedNicknames = new LinkedList<>();
        this.changedTeams = new LinkedList<>();
        this.nextTickTasks = new LinkedList<>();
        this.dataMap = new ConcurrentHashMap<>();
    }

    public static void init() {
        PlayerConnectCallback.EVENT.register(PlayerDataManager::onPlayerConnect);
        PlayerLeaveCallback.EVENT.register(PlayerDataManager::onPlayerLeave);
        PlayerDeathCallback.EVENT.register(PlayerDataManager::onPlayerDeath);
        PlayerRespawnCallback.EVENT.register(PlayerDataManager::onPlayerRespawn);
        ServerTickEvents.END_SERVER_TICK.register((MinecraftServer server) -> PlayerDataManager.getInstance().tick(server));
        ServerPlayConnectionEvents.JOIN.register(PlayerDataManager::onPlayerConnected);
    }

    public static final Event<PlayerDataManagerTickCallback> TICK_EVENT = EventFactory.createArrayBacked(PlayerDataManagerTickCallback.class,
        (listeners) -> (playerDataManager, server) -> {
            for (PlayerDataManagerTickCallback event : listeners) {
                event.onTick(playerDataManager, server);
                server.getOverworld().updateSleepingPlayers();
            }
        });

    private static void onPlayerConnected(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server) {
        if (CONFIG.ENABLE_MOTD) {
            var player = handler.getPlayer();
            var message = Placeholders.parseText(
                TextParserUtils.formatText(CONFIG.MOTD),
                PlaceholderContext.of(player)
            );
            player.getCommandSource().sendFeedback(message, false);
        }
    }

    public static PlayerDataManager getInstance() {
        return INSTANCE != null ? INSTANCE : new PlayerDataManager();
    }

    public void markNicknameDirty(PlayerData playerData) {
        changedNicknames.add(playerData);
    }

    public void markNicknameDirty(String playerName) {
        changedTeams.add(playerName);
    }

    public void tick(MinecraftServer server) {
        if (CONFIG.NICKNAMES_IN_PLAYER_LIST && server.getTicks() % (20*5) == 0) {
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
    private static void onPlayerConnect(ClientConnection connection, ServerPlayerEntity player) {
        PlayerData playerData = getInstance().loadPlayerData(player);
        ((ServerPlayerEntityAccess) player).setEcPlayerData(playerData);
        // Immediately save file. (Frankly not sure if this is actually necessary)
        playerData.markDirty();
        playerData.save();
    }

    private static void onPlayerLeave(ServerPlayerEntity player) {
        // Auto-saving should be handled by WorldSaveHandlerMixin. (PlayerData saves when MC server saves players)
        getInstance().unloadPlayerData(player);
    }

    private static void onPlayerRespawn(ServerPlayerEntity oldPlayerEntity, ServerPlayerEntity newPlayerEntity) {
        PlayerData pData = ((ServerPlayerEntityAccess) oldPlayerEntity).getEcPlayerData();
        pData.updatePlayer(newPlayerEntity);
        ((ServerPlayerEntityAccess) newPlayerEntity).setEcPlayerData(pData);
    }

    private static void onPlayerDeath(ServerPlayerEntity playerEntity, DamageSource damageSource) {
        PlayerData pData = ((ServerPlayerEntityAccess) playerEntity).getEcPlayerData();
        if (CONFIG.ALLOW_BACK_ON_DEATH)
            pData.setPreviousLocation(new MinecraftLocation(pData.getPlayer()));
    }

    // SET / ADD
    private PlayerData loadPlayerData(ServerPlayerEntity player) {
        PlayerData playerData = ((ServerPlayerEntityAccess) player).getEcPlayerData();
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