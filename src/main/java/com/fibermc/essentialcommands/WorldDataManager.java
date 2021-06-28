package com.fibermc.essentialcommands;

import com.fibermc.essentialcommands.commands.suggestions.ListSuggestion;
import com.fibermc.essentialcommands.types.MinecraftLocation;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.PersistentState;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;

public class WorldDataManager extends PersistentState {
    private HashMap<String, MinecraftLocation> warps;
    private MinecraftLocation spawnLocation;
    private Path saveDir;
    private File worldDataFile;

    private final String SPAWN_KEY = "spawn";
    private final String WARPS_KEY = "warps";

    public WorldDataManager() {
        warps = new HashMap<String, MinecraftLocation>();
        spawnLocation = null;
    }

    public void onServerStart(MinecraftServer server) {
        this.saveDir = server.getSavePath(WorldSavePath.ROOT).resolve("essentialcommands");
        try {
            Files.createDirectories(saveDir);
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.worldDataFile = saveDir.resolve("world_data.dat").toFile();

        try {
            boolean fileExisted = !worldDataFile.createNewFile();
            if (fileExisted && worldDataFile.length() > 0) {
                // if files was not JUST created, read data from it.
                this.fromNbt(NbtIo.readCompressed(worldDataFile).getCompound("data"));
            } else {
                this.markDirty();
                this.save();
            }
        } catch (IOException e) {
            EssentialCommands.log(Level.ERROR, String.format("An unexpected error occoured while loading the Essential Commands World Data file (Path: '%s')", worldDataFile.getPath()));
            e.printStackTrace();
        }
    }

    private File getDataFile() {
        return worldDataFile;
    }

    public void fromNbt(NbtCompound tag) {
        MinecraftLocation tempSpawnLocation = new MinecraftLocation(tag.getCompound(SPAWN_KEY));
        if (tempSpawnLocation.dim.getValue().getPath().isEmpty())
            this.spawnLocation = null;
        else
            this.spawnLocation = tempSpawnLocation;
        NbtCompound warpsNbt = tag.getCompound(WARPS_KEY);
        warpsNbt.getKeys().forEach((key) -> {
            warps.put(key, new MinecraftLocation(warpsNbt.getCompound(key)));
        });
    }

    public void save() {
        EssentialCommands.log(Level.INFO, "Saving world_data.dat (Spawn/Warps)...");
        super.save(this.worldDataFile);
        EssentialCommands.log(Level.INFO, "world_data.dat saved.");
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        // Spawn to NBT
        NbtElement spawnNbt;
        if (spawnLocation != null)
            spawnNbt = spawnLocation.asNbt();
        else
            spawnNbt = new NbtCompound();
        tag.put(SPAWN_KEY, spawnNbt);

                // Warps to NBT
        NbtCompound warpsNbt = new NbtCompound();
        warps.forEach((key, value) -> {
            warpsNbt.put(key, value.asNbt());
        });
        tag.put(WARPS_KEY, warpsNbt);

        return tag;
    }

    // Command Actions
    public void setWarp(String warpName, MinecraftLocation location) {
        warps.put(warpName, location);
        this.markDirty();
        this.save();
    }
    public boolean delWarp(String warpName) {
        MinecraftLocation prevValue = warps.remove(warpName);
        this.markDirty();
        this.save();
        return prevValue != null;
    }
    public MinecraftLocation getWarp(String warpName) {
        return warps.get(warpName);
    }

    public List<String> getWarpNames() {
        return this.warps.keySet().stream().toList();
    }

    public void setSpawn(MinecraftLocation location) {
        spawnLocation = location;
        this.markDirty();
        this.save();
    }
    public MinecraftLocation getSpawn() {
        return spawnLocation;
    }

}
