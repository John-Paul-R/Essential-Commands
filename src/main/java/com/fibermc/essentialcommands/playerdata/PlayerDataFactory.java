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

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public final class PlayerDataFactory {
    private PlayerDataFactory() {}

    private static PlayerData create(ServerPlayerEntity player, File playerDataFile) {
        PlayerData pData = new PlayerData(player, playerDataFile);

        boolean fileExisted = false;

        try {
            fileExisted = !playerDataFile.createNewFile();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        if (fileExisted && playerDataFile.length() != 0) {
            try {
                pData.fromNbt(NbtIo.readCompressed(playerDataFile.toPath(), NbtSizeTracker.ofUnlimitedBytes()));
            } catch (IOException e) {
                EssentialCommands.log(Level.WARN,
                    "Failed to load essential_commands player data for {%s}", player.getName().getString());
                e.printStackTrace();
            }
        } else {
            pData.markDirty();
            pData.save();
        }

        return pData;
    }

    /**
     * This is exclusively used with EssentialsXParser
     */
    public static PlayerData create(NamedLocationStorage homes, File saveFile) {
        String fileName = saveFile.getName();
        UUID playerUuid = UUID.fromString(fileName.substring(0, fileName.indexOf(".dat")));
        PlayerData pData = new PlayerData(playerUuid, saveFile);
        if (Files.exists(saveFile.toPath()) && saveFile.length() != 0) {
            try {
                NbtCompound nbtCompound3 = NbtIo.readCompressed(saveFile.toPath(), NbtSizeTracker.ofUnlimitedBytes());
                pData.fromNbt(nbtCompound3);
                // If a EC data already existed, the homes we just initialized the pData with (from paramater) just got overwritten.
                // Now, add them back if their keys do not already exist in the set we just loaded from EC save file.
                pData.homes.putAll(homes);
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

        pData.markDirty();
        return pData;
    }

    public static PlayerData create(ServerPlayerEntity player) {
        try {
            return create(player, getPlayerDataFile(player));
        } catch (IOException ex) {
            EssentialCommands.log(
                Level.ERROR,
                "Failed to create player data file for player with id '{}'. Player data may fail to save, or other unexpected behavior may occur.",
                player.getUuidAsString());
            EssentialCommands.LOGGER.error(ex);
        }
        return new PlayerData(player, null);
    }

    public static Path getPlayerDataDirectoryPath(MinecraftServer server) throws IOException {
        return FileUtil.getOrCreateWorldDirectory(server, "modplayerdata");
    }

    private static File getPlayerDataFile(ServerPlayerEntity player) throws IOException {
        return getPlayerDataDirectoryPath(player.getServer())
            .resolve(player.getUuidAsString() + ".dat")
            .toFile();
    }
}
