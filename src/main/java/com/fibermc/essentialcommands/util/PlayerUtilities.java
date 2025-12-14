package com.fibermc.essentialcommands.util;

import java.util.List;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class PlayerUtilities {
    private PlayerUtilities() {}

    public static boolean isNearAngryMonsters(ServerPlayer player) {
        var world = player.level();
        var pos = player.blockPosition();
        double boxHorizontalSize = 8.0;
        double boxHeight = 5.0;
        Vec3 vec3d = Vec3.atBottomCenterOf(pos);
        List<Monster> list = world
            .getEntitiesOfClass(
                Monster.class,
                new AABB(
                    vec3d.x() - boxHorizontalSize,
                    vec3d.y() - boxHeight,
                    vec3d.z() - boxHorizontalSize,
                    vec3d.x() + boxHorizontalSize,
                    vec3d.y() + boxHeight,
                    vec3d.z() + boxHorizontalSize
                ),
                (entity) -> entity.isPreventingPlayerRest(world, player)
            );

        return !list.isEmpty();
    }
}
