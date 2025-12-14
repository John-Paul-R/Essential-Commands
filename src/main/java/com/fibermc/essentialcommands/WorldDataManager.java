package com.fibermc.essentialcommands;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.fibermc.essentialcommands.types.MinecraftLocation;
import com.fibermc.essentialcommands.types.WarpLocation;
import com.fibermc.essentialcommands.types.WarpStorage;
import org.apache.logging.log4j.Level;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.LevelResource;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public class WorldDataManager extends SavedData {
    private Path saveDir;
    private File worldDataFile;
    private WorldData data;

    public WorldDataManager() {
        this.data = new WorldData();
    }

    public static WorldDataManager createForServer(MinecraftServer server)
    {
        var worldDataManager = new WorldDataManager();
        worldDataManager.onServerStart(server);
        return worldDataManager;
    }

    public void onServerStart(MinecraftServer server) {
        this.saveDir = server.getWorldPath(LevelResource.ROOT).resolve("essentialcommands");
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
                var tag = NbtIo.readCompressed(worldDataFile.toPath(), NbtAccounter.unlimitedHeap());
                // `data` was the main obj key in old mc PersistentState schema
                this.data = WorldData.fromNbt(tag.getCompound("data").orElse(tag));
                warpsLoadEvent.invoker().accept(this.data.warps());
            } else {
                this.setDirty();
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

    public final Event<Consumer<WarpStorage>> warpsLoadEvent = EventFactory.createArrayBacked(
        Consumer.class,
        (listeners) -> (warps) -> {
            for (Consumer<WarpStorage> event : listeners) {
                event.accept(warps);
            }
        });

    public void save() {
        EssentialCommands.log(Level.INFO, "Saving world_data.dat (Spawn/Warps)...");
        CompoundTag data = this.data.toNbt();
        try {
            NbtIo.writeCompressed(data, this.worldDataFile.toPath());
        } catch (IOException e) {
            EssentialCommands.LOGGER.error("Could not save data {}", this, e);
        }
        EssentialCommands.log(Level.INFO, "world_data.dat saved.");
    }

    // Command Actions
    public void setWarp(String warpName, MinecraftLocation location, boolean requiresPermission) throws CommandSyntaxException {
        this.data.warps().putCommand(warpName, new WarpLocation(
            location,
            requiresPermission ? warpName : null,
            warpName
        ));
        this.setDirty();
        this.save();
    }

    public boolean delWarp(String warpName) {
        MinecraftLocation prevValue = this.data.warps().remove(warpName);
        this.setDirty();
        this.save();
        return prevValue != null;
    }

    public WarpLocation getWarp(String warpName) {
        return this.data.warps().get(warpName);
    }

    public List<String> getWarpNames() {
        return this.data.warps().keySet().stream().toList();
    }

    public Stream<WarpLocation> getAccessibleWarps(ServerPlayer player) {
        var warpsStream = this.data.warps().values().stream();
        return (EssentialCommands.CONFIG.USE_PERMISSIONS_API
            ? warpsStream.filter(loc -> loc.hasPermission(player))
            : warpsStream);
    }

    public Set<Entry<String, WarpLocation>> getWarpEntries() {
        return this.data.warps().entrySet();
    }

    public void setSpawn(MinecraftLocation location) {
        this.data.setSpawn(location);
        this.setDirty();
        this.save();
    }

    public Optional<MinecraftLocation> getSpawn() {
        return this.data.getSpawn();
    }

}
