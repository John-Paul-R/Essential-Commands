package com.fibermc.joinpoints.types;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import com.fibermc.essentialcommands.types.MinecraftLocation;
import com.fibermc.essentialcommands.types.NamedMinecraftLocation;
import com.fibermc.joinpoints.codec.JoinpointCodecs;

import com.mojang.serialization.Codec;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

public class JoinpointLocation extends NamedMinecraftLocation {
    public static final Codec<JoinpointLocation> CODEC = JoinpointCodecs.JOINPOINT_LOCATION;

    private final UUID owner;
    private final boolean isGlobal;
    private final Set<UUID> sharedWith;

    private JoinpointLocation() {
        this.owner = null;
        this.isGlobal = false;
        this.sharedWith = new HashSet<>();
    }

    public JoinpointLocation(NamedMinecraftLocation location, UUID owner, boolean isGlobal, Set<UUID> sharedWith) {
        super(location, location.getName());
        this.owner = owner;
        this.isGlobal = isGlobal;
        this.sharedWith = new HashSet<>(sharedWith);
    }

    public JoinpointLocation(MinecraftLocation location, String name, UUID owner, boolean isGlobal, Set<UUID> sharedWith) {
        super(location, name);
        this.owner = owner;
        this.isGlobal = isGlobal;
        this.sharedWith = new HashSet<>(sharedWith);
    }

    public JoinpointLocation(
        RegistryKey<World> dim,
        double x,
        double y,
        double z,
        float headYaw,
        float pitch,
        Optional<String> name,
        UUID owner,
        boolean isGlobal,
        Set<UUID> sharedWith
    ) {
        super(dim, x, y, z, headYaw, pitch, name);
        this.owner = owner;
        this.isGlobal = isGlobal;
        this.sharedWith = new HashSet<>(sharedWith);
    }

    public static JoinpointLocation fromNbt(NbtCompound tag) {
        var result = CODEC.parse(NbtOps.INSTANCE, tag);

        if (result.isSuccess()) {
            return result.getOrThrow();
        }

        throw new RuntimeException("Failed to parse JoinpointLocation from NBT: " + result.error());
    }

    public static JoinpointLocation setName(JoinpointLocation value, String key) {
        value.name = key;
        return value;
    }

    @Override
    public NbtCompound asNbt() {
        return this.writeNbt(new NbtCompound());
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        return CODEC.encodeStart(NbtOps.INSTANCE, this)
            .getOrThrow()
            .asCompound()
            .orElseThrow();
    }

    public UUID getOwner() {
        return owner;
    }

    public boolean isGlobal() {
        return isGlobal;
    }

    public HashSet<UUID> getSharedWith() {
        return new HashSet<>(sharedWith);
    }

    public boolean canAccess(ServerPlayerEntity player) {
        if (isGlobal) return true;
        if (owner != null && owner.equals(player.getUuid())) return true;
        return sharedWith.contains(player.getUuid());
    }

    public void addSharedPlayer(UUID playerUuid) {
        if (!isGlobal) {
            sharedWith.add(playerUuid);
        }
    }

    public void removeSharedPlayer(UUID playerUuid) {
        sharedWith.remove(playerUuid);
    }
}
