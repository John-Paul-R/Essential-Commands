package com.fibermc.essentialcommands;

import com.fibermc.essentialcommands.types.MinecraftLocation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Objects;

/**
 * Teleporter
 */
public class PlayerTeleporter {

    public static void requestTeleport(PlayerData pData, MinecraftLocation dest) {
        ServerPlayerEntity player = pData.getPlayer();
        if (pData.getTpCooldown() < 0 || player.getServer().getPlayerManager().isOperator(player.getGameProfile())) {
            //send TP request to tpManager
        }

        teleport(pData, dest);
    }

    public static void teleport(PlayerData pData, MinecraftLocation dest) {//forceTeleport
        ServerPlayerEntity player = pData.getPlayer();
        pData.setPreviousLocation(new MinecraftLocation(player));

        player.teleport(
            player.getServer().getWorld(dest.dim),
            dest.pos.x, dest.pos.y, dest.pos.z,
            dest.headYaw, dest.pitch
        );
    }

}