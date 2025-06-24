package com.fibermc.essentialcommands;

import com.fibermc.essentialcommands.codec.Codecs;
import com.fibermc.essentialcommands.types.MinecraftLocation;
import com.fibermc.essentialcommands.types.WarpStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;

public class WorldData {
    private static final Codec<WorldData> CODEC = Codecs.WORLD_DATA;

    private @Nullable MinecraftLocation spawnLocation;
    private final @NotNull WarpStorage warps;

    public WorldData() {
        this.spawnLocation = null;
        this.warps = new WarpStorage();
    }

    public WorldData(@Nullable MinecraftLocation spawnLocation, @NotNull WarpStorage warps) {
        this.spawnLocation = spawnLocation;
        this.warps = warps;
    }

    public WarpStorage warps() {
        return this.warps;
    }

    public MinecraftLocation getSpawn() {
        return this.spawnLocation;
    }

    public void setSpawn(@Nullable MinecraftLocation spawn) {
        this.spawnLocation = spawn;
    }

    public static WorldData fromNbt(NbtCompound nbt) {
        return CODEC.parse(NbtOps.INSTANCE, nbt).getOrThrow();
    }

    public NbtCompound toNbt() {
        return CODEC.encodeStart(NbtOps.INSTANCE, this).getOrThrow().asCompound().orElseThrow();
    }
}
