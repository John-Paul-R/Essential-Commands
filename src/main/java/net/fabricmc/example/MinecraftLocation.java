package net.fabricmc.example;

import net.minecraft.server.world.ServerWorld;

public class MinecraftLocation {

    int x, y, z;
    ServerWorld world;

    public MinecraftLocation(int x, int y, int z, ServerWorld world) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
    }
    
}