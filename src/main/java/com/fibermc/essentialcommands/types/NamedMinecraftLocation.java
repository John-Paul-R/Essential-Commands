package com.fibermc.essentialcommands.types;

import java.util.Optional;

import com.fibermc.essentialcommands.codec.Codecs;

import com.mojang.serialization.Codec;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class NamedMinecraftLocation extends MinecraftLocation {
    public static final Codec<NamedMinecraftLocation> CODEC = Codecs.NAMED_MINECRAFT_LOCATION;

    protected String name;

    protected NamedMinecraftLocation() {}

    public NamedMinecraftLocation(MinecraftLocation location, String name) {
        super(
            location.dim(),
            location.pos().x,
            location.pos().y,
            location.pos().z,
            location.headYaw(),
            location.pitch()
        );

        this.name = name;
    }

    public NamedMinecraftLocation(
        ResourceKey<Level> dim,
        double x,
        double y,
        double z,
        float headYaw,
        float pitch,
        Optional<String> name
    ) {
        super(dim, x, y, z, headYaw, pitch);
        this.name = name.orElse(null);
    }

    public NamedMinecraftLocation(
        ResourceKey<Level> dim,
        Vec3 pos,
        float headYaw,
        float pitch,
        String name
    ) {
        super(dim, pos.x, pos.y, pos.z, headYaw, pitch);
        this.name = name;
    }

    public static NamedMinecraftLocation fromNbt(CompoundTag tag) {
        return CODEC.parse(NbtOps.INSTANCE, tag)
            .getOrThrow();
    }

    public CompoundTag writeNbt(CompoundTag tag) {
        return CODEC.encodeStart(NbtOps.INSTANCE, this)
            .getOrThrow()
            .asCompound()
            .orElseThrow();

    }

    public String getName() {
        return name;
    }
}
