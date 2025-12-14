package com.fibermc.essentialcommands.types;

import java.util.Optional;

import com.fibermc.essentialcommands.ECPerms;
import com.fibermc.essentialcommands.codec.Codecs;

import com.mojang.serialization.Codec;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public class WarpLocation extends NamedMinecraftLocation {
    public static final Codec<WarpLocation> CODEC = Codecs.WARP_LOCATION;
    private String permissionString;

    private WarpLocation() {}

    /**
     * @param permissionString The string permission node for the warp. Null for no required permisison.
     */
    public WarpLocation(NamedMinecraftLocation location, String permissionString) {
        super(location, location.getName());
        this.permissionString = permissionString;
    }

    public WarpLocation(MinecraftLocation location, String permissionString, String name) {
        super(location, name);
        this.permissionString = permissionString;
    }

    public WarpLocation(
        ResourceKey<Level> dim,
        double x,
        double y,
        double z,
        float headYaw,
        float pitch,
        Optional<String> name,
        Optional<String> permissionString
    ) {
        super(dim, x, y, z, headYaw, pitch, name);
        this.permissionString = permissionString.orElse(null);
    }

    public static WarpLocation fromNbt(CompoundTag tag) {
        var result = CODEC.parse(NbtOps.INSTANCE, tag);

        if (result.isSuccess()) {
            return result.getOrThrow();
        }

        throw new RuntimeException("Failed to parse WarpLocation from NBT: " + result.error());
    }

    public static WarpLocation setName(WarpLocation value, String key) {
        value.name = key;
        return value;
    }

    @Override
    public CompoundTag asNbt() {
        return this.writeNbt(new CompoundTag());
    }

    @Override
    public CompoundTag writeNbt(CompoundTag tag) {
        return CODEC.encodeStart(NbtOps.INSTANCE, this)
            .getOrThrow()
            .asCompound()
            .orElseThrow();
    }

    public String getPermissionString() {
        return permissionString;
    }

    public boolean hasPermission(ServerPlayer player) {
        return permissionString == null || ECPerms.check(
            player.createCommandSourceStack(),
            String.format("%s.%s", ECPerms.Registry.warp_tp_named, permissionString));
    }
}
