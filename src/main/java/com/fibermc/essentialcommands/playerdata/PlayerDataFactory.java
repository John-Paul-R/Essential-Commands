package com.fibermc.essentialcommands.playerdata;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import com.fibermc.essentialcommands.EssentialCommands;
import com.fibermc.essentialcommands.types.NamedLocationStorage;
import com.fibermc.essentialcommands.util.FileUtil;
import org.apache.logging.log4j.Level;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class PlayerDataFactory {
    private PlayerDataFactory() {}

    private static PlayerData create(ServerPlayer player, File playerDataFile) {
        boolean fileExisted = false;

        try {
            fileExisted = !playerDataFile.createNewFile();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        if (fileExisted && playerDataFile.length() != 0) {
            try {
                var tag = NbtIo.readCompressed(playerDataFile.toPath(), NbtAccounter.unlimitedHeap());

                tag = PlayerData.fixData(tag);

                var playerData = PlayerData.CODEC.parse(
                    NbtOps.INSTANCE,
                    tag
                ).getOrThrow();
                playerData.initializeRuntimeState(player, playerDataFile);

                return playerData;
            } catch (IOException e) {
                EssentialCommands.log(
                    Level.WARN,
                    "Failed to load essential_commands player data for {%s}", player.getName().getString());
                e.printStackTrace();
//                Files.copy(playerDataFile.toPath(), Path.of(playerDataFile.toPath() + ".bk"));
                return new PlayerData(player, playerDataFile);
            }
        } else {
            PlayerData pData = new PlayerData(player, playerDataFile);
            pData.setDirty();
            pData.save();
            return pData;
        }
    }

    /**
     * This is exclusively used with EssentialsXParser
     */
    public static PlayerData create(NamedLocationStorage homes, File saveFile) {
        String fileName = saveFile.getName();
        UUID playerUuid = UUID.fromString(fileName.substring(0, fileName.indexOf(".dat")));

        if (PlayerDataManager.exists()) {
            PlayerData activePlayerData = PlayerDataManager.getInstance().getByUuid(playerUuid);
            if (activePlayerData != null) {
                activePlayerData.homes.putAll(homes);
                activePlayerData.setDirty();
                return activePlayerData;
            }
        }
        PlayerData playerData = null;
        if (Files.exists(saveFile.toPath()) && saveFile.length() != 0) {
            try {
                CompoundTag tag = NbtIo.readCompressed(saveFile.toPath(), NbtAccounter.unlimitedHeap());
                // Handle legacy "data" wrapper if present
                CompoundTag dataTag = tag.getCompound("data").orElse(tag);

                playerData = PlayerData.CODEC.parse(
                    NbtOps.INSTANCE,
                    dataTag
                ).getOrThrow();
                playerData.initializeSaveFileField(saveFile);
                // If a EC data already existed, the homes we just initialized the pData with (from paramater) just got overwritten.
                // Now, add them back if their keys do not already exist in the set we just loaded from EC save file.
                playerData.homes.putAll(homes);
                //Testing:

            } catch (IOException e) {
                EssentialCommands.log(Level.WARN, "Failed to load essential_commands player data for {"
                    + playerUuid
                    + "}");
                e.printStackTrace();
            }
        } else {
            try {
                saveFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (playerData == null) {
            playerData = new PlayerData(saveFile);
            playerData.homes.putAll(homes);
        }

        playerData.setDirty();
        return playerData;
    }

    public static PlayerData create(ServerPlayer player) {
        try {
            return create(player, getPlayerDataFile(player));
        } catch (IOException ex) {
            EssentialCommands.log(
                Level.ERROR,
                "Failed to create player data file for player with id '{}'. Player data may fail to save, or other unexpected behavior may occur.",
                player.getStringUUID());
            EssentialCommands.LOGGER.error(ex);
        }
        return new PlayerData(player, null);
    }

    public static Path getPlayerDataDirectoryPath(MinecraftServer server) throws IOException {
        return FileUtil.getOrCreateWorldDirectory(server, "modplayerdata");
    }

    private static File getPlayerDataFile(ServerPlayer player) throws IOException {
        return getPlayerDataDirectoryPath(player.level().getServer())
            .resolve(player.getStringUUID() + ".dat")
            .toFile();
    }
}
