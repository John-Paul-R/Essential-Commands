package com.fibermc.essentialcommands;

import java.util.Optional;

import com.fibermc.essentialcommands.codec.Codecs;
import com.fibermc.essentialcommands.datafixer.WorldDataDataFixer;
import com.fibermc.essentialcommands.types.MinecraftLocation;
import com.fibermc.essentialcommands.types.WarpStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;

public class WorldData {
    private @Nullable MinecraftLocation spawnLocation;
    private final @NotNull WarpStorage warps;

    WorldData() {
        this.spawnLocation = null;
        this.warps = new WarpStorage();
    }

    WorldData(Optional<MinecraftLocation> spawnLocation, @NotNull WarpStorage warps) {
        this.spawnLocation = spawnLocation.orElse(null);
        this.warps = warps;
    }

    public WarpStorage warps() {
        return this.warps;
    }

    public Optional<MinecraftLocation> getSpawn() {
        return Optional.ofNullable(this.spawnLocation);
    }

    public void setSpawn(@Nullable MinecraftLocation spawn) {
        this.spawnLocation = spawn;
    }

    public static WorldData fromNbt(NbtCompound nbt) {
        // Handle legacy "data" wrapper if present
        nbt = nbt.getCompound("data").orElse(nbt);
        nbt = WorldDataDataFixer.createDataFixer().build().fixer().update(
            WorldDataDataFixer.TYPE,
            new Dynamic<NbtElement>(NbtOps.INSTANCE, nbt),
            nbt.getInt(SCHEMA_VERSION_KEY, 0),
            WorldData.SCHEMA_VERSION
        ).getValue().asCompound().orElseThrow();

        return CODEC.parse(NbtOps.INSTANCE, nbt).getOrThrow();
    }

    public NbtCompound toNbt() {
        var data = CODEC.encodeStart(NbtOps.INSTANCE, this).getOrThrow().asCompound().orElseThrow();
        data.putInt(SCHEMA_VERSION_KEY, SCHEMA_VERSION);
        return data;
    }

    private static final String SCHEMA_VERSION_KEY = "_sv";
    private static final int SCHEMA_VERSION = 1;
    public static final Codec<WorldData> CODEC = RecordCodecBuilder.create(instance ->
        instance.group(
            Codecs.MINECRAFT_LOCATION.optionalFieldOf("spawn").forGetter(WorldData::getSpawn),
            Codecs.WARP_STORAGE.fieldOf("warps").forGetter(WorldData::warps)
        ).apply(instance, WorldData::new)
    );
}
