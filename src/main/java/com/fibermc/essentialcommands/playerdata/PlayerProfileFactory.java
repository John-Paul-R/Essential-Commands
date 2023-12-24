package com.fibermc.essentialcommands.playerdata;

import java.io.File;
import java.io.IOException;

import com.fibermc.essentialcommands.EssentialCommands;
import com.fibermc.essentialcommands.util.FileUtil;
import org.apache.logging.log4j.Level;

import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.server.network.ServerPlayerEntity;

public final class PlayerProfileFactory {
    private PlayerProfileFactory() {}

    private static PlayerProfile create(ServerPlayerEntity player, File playerDataFile) {
        PlayerProfile pData = new PlayerProfile(player, playerDataFile);

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
                EssentialCommands.log(
                    Level.WARN,
                    "Failed to load essential_commands player profile for {}", player.getName().getString());
                e.printStackTrace();
            }
        } else {
            pData.markDirty();
            pData.save();
        }

        return pData;
    }

    public static PlayerProfile create(ServerPlayerEntity player) {
        try {
            return create(player, getPlayerProfileFile(player));
        } catch (IOException ex) {
            EssentialCommands.log(
                Level.ERROR,
                "Failed to create player profile file for player with id '{}'. Player profile may fail to save, or other unexpected behavior may occur.",
                player.getUuidAsString());
            EssentialCommands.LOGGER.error(ex);
        }
        return new PlayerProfile(player, null);
    }

    private static File getPlayerProfileFile(ServerPlayerEntity player) throws IOException {
        return FileUtil.getOrCreateWorldDirectory(player.getServer(), "ec_player_profiles")
            .resolve(player.getUuidAsString() + ".dat")
            .toFile();
    }
}
