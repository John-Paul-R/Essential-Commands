package com.fibermc.essentialcommands;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.WorldSavePath;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PlayerDataFactory {

    public static PlayerData create(ServerPlayerEntity player, File saveFile) {
        PlayerData pData = new PlayerData(player, saveFile);
        try {
            NbtCompound NbtCompound3 = NbtIo.readCompressed(new FileInputStream(saveFile));
            pData.fromNbt(NbtCompound3);
            //Testing:
            pData.markDirty();

        } catch (IOException e) {
            EssentialCommands.log(Level.WARN, "Failed to load essential_commands player data for {"+player.getName().getString()+"}");
            e.printStackTrace();
        }

        return pData;
    }
    public static PlayerData create(ServerPlayerEntity player) {
        return create(player, getPlayerDataFile(player));
    }

    private static File getPlayerDataFile(ServerPlayerEntity player) {
        String pUuid = player.getUuidAsString();

        //Path mainDirectory = player.getServer().getRunDirectory().toPath();
        Path dataDirectoryPath;
        File playerDataFile = null;
        try {
            try {
                dataDirectoryPath = Files.createDirectories(player.getServer().getSavePath(WorldSavePath.ROOT).resolve("modplayerdata"));
            } catch (NullPointerException e){
                dataDirectoryPath = Files.createDirectories(Paths.get("./world/modplayerdata/"));
                EssentialCommands.log(Level.WARN, "Session save path could not be found. Defaulting to ./world/modplayerdata");
            }
            playerDataFile = dataDirectoryPath.resolve(pUuid+".dat").toFile();
            playerDataFile.createNewFile();
//            if (playerDataFile.createNewFile() || playerDataFile.length()==0) {//creates file and returns true only if file did not exist, otherwise returns false
//                //Initialize file if just created
//                pData.markDirty();
//                pData.save();
//            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return playerDataFile;
    }
}
