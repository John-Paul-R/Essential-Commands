package com.fibermc.essentialcommands;

import com.fibermc.essentialcommands.events.PlayerConnectCallback;
import com.fibermc.essentialcommands.events.PlayerDeathCallback;
import com.fibermc.essentialcommands.events.PlayerLeaveCallback;
import com.fibermc.essentialcommands.events.PlayerRespawnCallback;
import com.fibermc.essentialcommands.types.MinecraftLocation;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.network.ServerPlayerEntity;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataManager {

    private ConcurrentHashMap<UUID, PlayerData> dataMap;
    public PlayerDataManager() {
        this.dataMap = new ConcurrentHashMap<>();
        PlayerConnectCallback.EVENT.register(this::onPlayerConnect);
        PlayerLeaveCallback.EVENT.register(this::onPlayerLeave);
        PlayerDeathCallback.EVENT.register(this::onPlayerDeath);
        PlayerRespawnCallback.EVENT.register(this::onPlayerRespawn);
    }

    private void onPlayerRespawn(ServerPlayerEntity serverPlayerEntity) {
        PlayerData pData = this.getOrCreate(serverPlayerEntity);
        pData.updatePlayer(serverPlayerEntity);
    }

    private void onPlayerDeath(UUID playerID, DamageSource damageSource) {
        PlayerData pData = getPlayerFromUUID(playerID);
        //EssentialCommands.log(Level.DEBUG, "Worked2 " + pData.getPlayer().getGameProfile().getName());
        pData.setPreviousLocation(new MinecraftLocation(pData.getPlayer()));
    }

    public void addPlayerData(ServerPlayerEntity player) {

        dataMap.put(player.getUuid(), new PlayerData(player.getUuidAsString(), player));
    }
    public void addPlayerData(PlayerData pData) {

        dataMap.put(pData.getPlayer().getUuid(), pData);
    }

    public PlayerData getOrCreate(ServerPlayerEntity player) {

        UUID uuid = player.getUuid();
        if (!dataMap.containsKey(uuid)) {
            addPlayerData(player);
        }
        return dataMap.get(uuid);
    }
    PlayerData getPlayerFromUUID(UUID playerID) {
        return dataMap.get(playerID);
    }

//    ConcurrentHashMap<UUID, PlayerData> getDataMap() {
//        return this.dataMap;
//    }

    private void unloadPlayerData(ServerPlayerEntity player) {
        this.dataMap.remove(player.getUuid());
    }
    public void onPlayerConnect(ClientConnection connection, ServerPlayerEntity player) {
        try {
            loadPlayerData(player);
        } catch (IOException e) {
            //TODO: handle exception
        }
    }

    private File getPlayerDataFile(ServerPlayerEntity player) {
        String pUuid = player.getUuidAsString();

        //Path mainDirectory = player.getServer().getRunDirectory().toPath();
        Path dataDirectoryPath;
        File playerDataFile = null;
        try {
            dataDirectoryPath = Files.createDirectories(Paths.get("./world/modplayerdata/"));
            playerDataFile = dataDirectoryPath.resolve(pUuid+".dat").toFile();
            if (playerDataFile.createNewFile() || playerDataFile.length()==0) {//creates file and returns true only if file did not exist, otherwise returns false
                //Initialize file if just created
                initPlayerDataFile(player, playerDataFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return playerDataFile;
    }

    private void initPlayerDataFile(ServerPlayerEntity player, File playerDataFile) {
        PlayerData pData = this.getOrCreate(player);
        pData.markDirty();
        pData.save(playerDataFile);
    }

    PlayerData loadPlayerData(ServerPlayerEntity player) throws IOException {
        String pUuid = player.getUuidAsString();

        File playerDataFile = getPlayerDataFile(player);

        PlayerData pData = new PlayerData(pUuid, player);

        PushbackInputStream pushbackInputStream = new PushbackInputStream(new FileInputStream(playerDataFile), 2);
        DataInputStream dataInputStream = new DataInputStream(pushbackInputStream);

        CompoundTag compoundTag3 = new CompoundTag();
        Throwable var8 = null;
        if (this.inputIsCompressed(pushbackInputStream)) {
            compoundTag3 = NbtIo.readCompressed(pushbackInputStream);
         } else {
            try {
                compoundTag3 = NbtIo.read(dataInputStream);
            } catch (Throwable var31) {
                var8 = var31;
                throw var31;
            } finally {
                if (dataInputStream != null) {
                    if (var8 != null) {
                        try {
                        dataInputStream.close();
                        } catch (Throwable var30) {
                        var8.addSuppressed(var30);
                        }
                    } else {
                        dataInputStream.close();
                    }
                }

            }
        }
        //EssentialCommands.log(Level.INFO, "TagData:\n-=-=-=-=-=-\n"+compoundTag3.asString()+"\n-=-=-=-=-=-=-=-");
        pData.fromTag(compoundTag3);
        //Testing:
        pData.markDirty();
        addPlayerData(pData);
        return pData;
    }
    //Compressed NBT Reader
    private boolean inputIsCompressed(PushbackInputStream pushbackInputStream) throws IOException {
        byte[] bs = new byte[2];
        boolean bl = false;
        int i = pushbackInputStream.read(bs, 0, 2);
        if (i == 2) {
           int j = (bs[1] & 255) << 8 | bs[0] & 255;
           if (j == 35615) {
              bl = true;
           }
        }
  
        if (i != 0) {
           pushbackInputStream.unread(bs, 0, i);
        }
  
        return bl;
    }

    public void savePlayerData(ServerPlayerEntity player) {
        this.getOrCreate(player).save(this.getPlayerDataFile(player));
    }

    public void onPlayerLeave(ServerPlayerEntity player) {
        try {
            savePlayerData(player);

        } catch (Exception e) {
            //TODO: handle exception
        }

        unloadPlayerData(player);
    }

    //-=-=-=-=-=-=-=-=-=-=-=-



    
}