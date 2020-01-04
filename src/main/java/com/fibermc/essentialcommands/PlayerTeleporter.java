package com.fibermc.essentialcommands;

import com.fibermc.essentialcommands.types.MinecraftLocation;
import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Teleporter
 */
public class PlayerTeleporter {

    static void teleport(PlayerData pData, MinecraftLocation dest) {
        ServerPlayerEntity player = pData.getPlayer();
        MinecraftLocation initialPosition = new MinecraftLocation(player);
        pData.setPreviousLocation(initialPosition);
        player.teleport(player.getServer().getWorld(dest.dim), dest.x, dest.y, dest.z, dest.headYaw, dest.pitch);
    }

}