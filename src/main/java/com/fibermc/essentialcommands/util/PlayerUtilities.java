package com.fibermc.essentialcommands.util;

import java.util.List;

import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public final class PlayerUtilities {
    private PlayerUtilities() {}

    public static boolean isNearAngryMonsters(ServerPlayerEntity player) {
        var world = player.getServerWorld();
        var pos = player.getBlockPos();
        double boxHorizontalSize = 8.0;
        double boxHeight = 5.0;
        Vec3d vec3d = Vec3d.ofBottomCenter(pos);
        List<HostileEntity> list = world
            .getEntitiesByClass(
                HostileEntity.class,
                new Box(
                    vec3d.getX() - boxHorizontalSize,
                    vec3d.getY() - boxHeight,
                    vec3d.getZ() - boxHorizontalSize,
                    vec3d.getX() + boxHorizontalSize,
                    vec3d.getY() + boxHeight,
                    vec3d.getZ() + boxHorizontalSize
                ),
                (entity) -> entity.isAngryAt(world, player)
            );

        return !list.isEmpty();
    }
}
