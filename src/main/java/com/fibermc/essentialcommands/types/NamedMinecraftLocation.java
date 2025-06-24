package com.fibermc.essentialcommands.types;

import java.util.Optional;

import com.fibermc.essentialcommands.codec.Codecs;

import com.mojang.serialization.Codec;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

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
        RegistryKey<World> dim,
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
        RegistryKey<World> dim,
        Vec3d pos,
        float headYaw,
        float pitch,
        String name
    ) {
        super(dim, pos.x, pos.y, pos.z, headYaw, pitch);
        this.name = name;
    }

    public static NamedMinecraftLocation fromNbt(NbtCompound tag) {
        return CODEC.parse(NbtOps.INSTANCE, tag)
            .getOrThrow();
    }

    public NbtCompound writeNbt(NbtCompound tag) {
        return CODEC.encodeStart(NbtOps.INSTANCE, this)
            .getOrThrow()
            .asCompound()
            .orElseThrow();

    }

    public String getName() {
        return name;
    }
}
