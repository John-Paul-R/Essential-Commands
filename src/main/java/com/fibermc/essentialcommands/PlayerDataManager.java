package com.fibermc.essentialcommands;

import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.config.Config;
import com.fibermc.essentialcommands.events.PlayerConnectCallback;
import com.fibermc.essentialcommands.events.PlayerDeathCallback;
import com.fibermc.essentialcommands.events.PlayerLeaveCallback;
import com.fibermc.essentialcommands.events.PlayerRespawnCallback;
import com.fibermc.essentialcommands.types.MinecraftLocation;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class PlayerDataManager {

    private final ConcurrentHashMap<UUID, PlayerData> dataMap;
    private List<PlayerData> changedNicknames;
    private static PlayerDataManager INSTANCE;

    public PlayerDataManager() {
        INSTANCE = this;
        this.changedNicknames = new LinkedList<>();
        this.dataMap = new ConcurrentHashMap<>();
    }

    public static void init() {
        PlayerConnectCallback.EVENT.register(PlayerDataManager::onPlayerConnect);
        PlayerLeaveCallback.EVENT.register(PlayerDataManager::onPlayerLeave);
        PlayerDeathCallback.EVENT.register(PlayerDataManager::onPlayerDeath);
        PlayerRespawnCallback.EVENT.register(PlayerDataManager::onPlayerRespawn);
        ServerTickEvents.END_SERVER_TICK.register((MinecraftServer server) -> PlayerDataManager.getInstance().tick(server));
    }

    public static PlayerDataManager getInstance() {
        return INSTANCE;
    }

    public void markNicknameDirty(PlayerData playerData) {
        changedNicknames.add(playerData);
    }
    public void tick(MinecraftServer server) {

        if (Config.NICKNAMES_IN_PLAYER_LIST && server.getTicks() % 600 == 0) {
            if (this.changedNicknames.size() > 0) {
                List<ServerPlayerEntity> changedNicknamePlayers = changedNicknames.stream().map(PlayerData::getPlayer).collect(Collectors.toList());
                PlayerListS2CPacket playerListPacket = new PlayerListS2CPacket(
                    PlayerListS2CPacket.Action.UPDATE_DISPLAY_NAME,
                    changedNicknamePlayers
                );
                server.getPlayerManager().getPlayerList().forEach(playerEntity -> {
                    playerEntity.networkHandler.sendPacket(playerListPacket);
                });
                this.changedNicknames.clear();
            }

        }

    }

    // EVENTS
    public static void onPlayerConnect(ClientConnection connection, ServerPlayerEntity player) {
        PlayerData playerData = INSTANCE.addPlayerData(player);
        ((ServerPlayerEntityAccess) player).setEcPlayerData(playerData);
        INSTANCE.initPlayerDataFile(player);
    }

    public static void onPlayerLeave(ServerPlayerEntity player) {
        // Auto-saving should be handled by WorldSaveHandlerMixin. (PlayerData saves when MC server saves players)
        INSTANCE.unloadPlayerData(player);
    }

    private static void onPlayerRespawn(ServerPlayerEntity oldPlayerEntity, ServerPlayerEntity newPlayerEntity) {
        PlayerData pData = ((ServerPlayerEntityAccess) oldPlayerEntity).getEcPlayerData();
        pData.updatePlayer(newPlayerEntity);
        ((ServerPlayerEntityAccess) newPlayerEntity).setEcPlayerData(pData);
    }

    private static void onPlayerDeath(ServerPlayerEntity playerEntity, DamageSource damageSource) {
        PlayerData pData = ((ServerPlayerEntityAccess) playerEntity).getEcPlayerData();
        if (Config.ALLOW_BACK_ON_DEATH)
            pData.setPreviousLocation(new MinecraftLocation(pData.getPlayer()));
    }

    // SET / ADD
    public PlayerData addPlayerData(ServerPlayerEntity player) {
        PlayerData playerData = PlayerDataFactory.create(player);
        dataMap.put(player.getUuid(), playerData);
        return playerData;
    }

    public PlayerData getPlayerData(ServerPlayerEntity player) {
        PlayerData playerData = dataMap.get(player.getUuid());

        if (playerData == null) {
            throw new NullPointerException(String.format("dataMap returned null for player with uuid %s", player.getUuid().toString()));
        }
        return playerData;
    }
    PlayerData getPlayerFromUUID(UUID playerID) {
        return dataMap.get(playerID);
    }

    // SAVE / LOAD
    private void unloadPlayerData(ServerPlayerEntity player) {
        this.dataMap.remove(player.getUuid());
    }

    private void initPlayerDataFile(ServerPlayerEntity player) {
        PlayerData pData = this.getPlayerData(player);
        pData.markDirty();
        pData.save();
    }

    public Collection<PlayerData> getAllPlayerData() {
        return dataMap.values();
    }

    /**
     * Case insentitive
     */
    public List<PlayerData> getPlayerDataMatchingNickname(String nickname) {
        return dataMap.values().stream()
            .filter(playerData -> playerData.getNickname().getString().equalsIgnoreCase(nickname))
            .collect(Collectors.toList());
    }
}