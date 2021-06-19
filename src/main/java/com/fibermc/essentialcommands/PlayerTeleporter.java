package com.fibermc.essentialcommands;

import com.fibermc.essentialcommands.types.MinecraftLocation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Teleporter
 */
public class PlayerTeleporter {

    public static void requestTeleport(PlayerData pData, MinecraftLocation dest) {
        ServerPlayerEntity player = pData.getPlayer();
        if (pData.getTpCooldown() < 0 || player.getServer().getPlayerManager().isOperator(player.getGameProfile())) {
            //send TP request to tpManager
        }
        MinecraftLocation initialPosition = new MinecraftLocation(player);
        pData.setPreviousLocation(initialPosition);
        player.teleport(player.getServer().getWorld(dest.dim), dest.x, dest.y, dest.z, dest.headYaw, dest.pitch);
    }

    public static void teleport(PlayerData pData, MinecraftLocation dest) {//forceTeleport
        ServerPlayerEntity player = pData.getPlayer();
        MinecraftLocation initialPosition = new MinecraftLocation(player);
        pData.setPreviousLocation(initialPosition);
        player.teleport(player.getServer().getWorld(dest.dim), dest.x, dest.y, dest.z, dest.headYaw, dest.pitch);
    }

}