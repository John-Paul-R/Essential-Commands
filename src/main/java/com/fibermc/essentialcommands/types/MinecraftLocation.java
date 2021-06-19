package com.fibermc.essentialcommands.types;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;


public class MinecraftLocation {

    public Vec3d pos;
    public float pitch, headYaw;
    public RegistryKey<World> dim;

    public MinecraftLocation(RegistryKey<World>  dim, double x, double y, double z) {
        this.dim = dim;
        this.pos = new Vec3d(x, y, z);
        this.pitch = 0f;
        this.headYaw = 0f;
        //todo world.getPersistentStateManager().
    }

    public MinecraftLocation(RegistryKey<World>  dim, double x, double y, double z, float headYaw, float pitch) {
        this.dim = dim;
        this.pos = new Vec3d(x, y, z);
        this.headYaw = headYaw;
        this.pitch = pitch; 
    }

    public MinecraftLocation(ServerPlayerEntity player) {
        this.dim = player.getServerWorld().getRegistryKey();
        this.pos = Vec3d.ZERO.add(player.getPos());
        this.headYaw = player.getHeadYaw();
        this.pitch = player.getPitch();
    }

    public MinecraftLocation(NbtCompound tag) {
        this.dim = RegistryKey.of(
            Registry.WORLD_KEY,
            Identifier.tryParse(tag.getString("WorldRegistryKey"))
        );
        this.pos = new Vec3d(
            tag.getDouble("x"),
            tag.getDouble("y"),
            tag.getDouble("z")
        );
        this.headYaw = tag.getFloat("headYaw");
        this.pitch = tag.getFloat("pitch");
    }
    public NbtCompound asNbt() {
        return this.writeNbt(new NbtCompound());
    }
    public NbtCompound writeNbt(NbtCompound tag) {
        tag.putString("WorldRegistryKey", dim.getValue().toString());
        tag.putDouble("x", pos.x);
        tag.putDouble("y", pos.y);
        tag.putDouble("z", pos.z);
        tag.putFloat("headYaw", headYaw);
        tag.putFloat("pitch", pitch);

        return tag;
    }
    
}