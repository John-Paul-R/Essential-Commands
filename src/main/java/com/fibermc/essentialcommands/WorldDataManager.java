package com.fibermc.essentialcommands;

import com.fibermc.essentialcommands.commands.suggestions.ListSuggestion;
import com.fibermc.essentialcommands.types.MinecraftLocation;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtNull;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.PersistentState;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

public class WorldDataManager extends PersistentState {
    private HashMap<String, MinecraftLocation> warps;
    private MinecraftLocation spawnLocation;
    private Path saveDir;
    private File worldDataFile;
    public WorldDataManager() {
        warps = new HashMap<String, MinecraftLocation>();
        spawnLocation = null;
    }

    public void init(MinecraftServer server) {
        this.saveDir = server.getSavePath(WorldSavePath.ROOT).resolve("essentialcommands");
        try {
            Files.createDirectories(saveDir);
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.worldDataFile = saveDir.resolve("world_data.dat").toFile();

        try {
            if (worldDataFile.createNewFile() || worldDataFile.length()==0) {//creates file and returns true only if file did not exist, otherwise returns false
                //Initialize file if just created
                this.save();
            } else {
                this.fromNbt(NbtIo.readCompressed(worldDataFile).getCompound("data"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File getDataFile() {
        return worldDataFile;
    }

    public void fromNbt(NbtCompound tag) {
        this.spawnLocation = new MinecraftLocation(tag.getCompound("spawnLocation"));
        NbtCompound warpsNbt = tag.getCompound("warps");
        warpsNbt.getKeys().forEach((key) -> {
            warps.put(key, new MinecraftLocation(warpsNbt.getCompound(key)));
        });
    }

    public void save() {
        super.save(this.worldDataFile);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag) {

        // Spawn to NBT
        NbtElement spawnNbt;
        try {
            spawnNbt = spawnLocation.asNbt();
        } catch (NullPointerException e) {
            spawnNbt = NbtNull.INSTANCE;
        }
        tag.put("spawnLocation", spawnNbt);

        // Warps to NBT
        NbtCompound warpsNbt = new NbtCompound();
        warps.forEach((key, value) -> {
            warpsNbt.put(key, value.asNbt());
        });
        tag.put("warps", warpsNbt);

        return tag;
    }

    // Command Actions
    public void setWarp(String warpName, MinecraftLocation location) {
        warps.put(warpName, location);
        this.markDirty();
        this.save();
    }
    public void delWarp(String warpName) {
        warps.remove(warpName);
        this.markDirty();
        this.save();
    }
    public MinecraftLocation getWarp(String warpName) {
        return warps.get(warpName);
    }
    public SuggestionProvider<ServerCommandSource> getWarpSuggestions() {
        return (context, builder) -> ListSuggestion.getSuggestionsBuilder(builder,
            this.warps.keySet().stream().toList()
        );
    }

    public void setSpawn(MinecraftLocation location) {
        spawnLocation = location;
    }
    public MinecraftLocation getSpawn() {
        return spawnLocation;
    }

}
