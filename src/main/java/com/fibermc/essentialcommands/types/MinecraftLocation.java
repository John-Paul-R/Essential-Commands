package com.fibermc.essentialcommands.types;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;


public class MinecraftLocation {

    public double x, y, z;
    public float pitch, headYaw;
    public RegistryKey<World> dim;

    public MinecraftLocation(RegistryKey<World>  dim, double x, double y, double z) {
        this.dim = dim;
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = 0f;
        this.headYaw = 0f;
        //todo world.getPersistentStateManager().
    }

    public MinecraftLocation(RegistryKey<World>  dim, double x, double y, double z, float headYaw, float pitch) {
        this.dim = dim;
        this.x = x;
        this.y = y;
        this.z = z;
        this.headYaw = headYaw;
        this.pitch = pitch; 
    }

    public MinecraftLocation(ServerPlayerEntity player) {
        this.dim = player.getServerWorld().getRegistryKey();
        this.x = player.getX();
        this.y = player.getY();
        this.z = player.getZ();
        this.headYaw = player.headYaw;
        this.pitch = player.getPitch();
    }

    public MinecraftLocation(NbtCompound tag) {
        this.dim = RegistryKey.of(
            Registry.WORLD_KEY,
            Identifier.tryParse(tag.getString("WorldRegistryKey"))
        );
        this.x = tag.getDouble("x");
        this.y = tag.getDouble("y");
        this.z = tag.getDouble("z");
        this.headYaw = tag.getFloat("headYaw");
        this.pitch = tag.getFloat("pitch");
    }
    public NbtCompound asNbt() {
        return this.writeNbt(new NbtCompound());
    }
    public NbtCompound writeNbt(NbtCompound tag) {
        tag.putString("WorldRegistryKey", dim.getValue().toString());
        tag.putDouble("x", x);
        tag.putDouble("y", y);
        tag.putDouble("z", z);
        tag.putFloat("headYaw", headYaw);
        tag.putFloat("pitch", pitch);

        return tag;
    }
    
}