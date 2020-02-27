package com.fibermc.essentialcommands.types;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.dimension.DimensionType;

public class MinecraftLocation {

    public double x, y, z;
    public float pitch, headYaw;
    public DimensionType dim;

    public MinecraftLocation(DimensionType dim, double x, double y, double z) {
        this.dim = dim;
        this.x = x;
        this.y = y;
        this.z = z;
        this.pitch = 0f;
        this.headYaw = 0f;
        //todo world.getPersistentStateManager().
    }

    public MinecraftLocation(DimensionType dim, double x, double y, double z, float headYaw, float pitch) {
        this.dim = dim;
        this.x = x;
        this.y = y;
        this.z = z;
        this.headYaw = headYaw;
        this.pitch = pitch; 
    }

    public MinecraftLocation(ServerPlayerEntity player) {
        this.dim = player.getServerWorld().getDimension().getType();
        this.x = player.getX();
        this.y = player.getY();
        this.z = player.getZ();
        this.headYaw = player.headYaw;
        this.pitch = player.pitch;
    }

    public MinecraftLocation(CompoundTag tag) {
        this.dim = DimensionType.byId(new Identifier(tag.getString("DimensionType")));
        this.x = tag.getDouble("x");
        this.y = tag.getDouble("y");
        this.z = tag.getDouble("z");
        this.headYaw = tag.getFloat("headYaw");
        this.pitch = tag.getFloat("pitch");
    }

    public CompoundTag toTag(CompoundTag tag) {
        tag.putString("DimensionType", DimensionType.getId(dim).toString());
        tag.putDouble("x", x);
        tag.putDouble("y", y);
        tag.putDouble("z", z);
        tag.putFloat("headYaw", headYaw);
        tag.putFloat("pitch", pitch);

        return tag;
    }
    
}