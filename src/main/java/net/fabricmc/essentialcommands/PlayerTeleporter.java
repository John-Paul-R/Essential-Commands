package net.fabricmc.essentialcommands;

import net.minecraft.server.network.ServerPlayerEntity;

/**
 * Teleporter
 */
public class PlayerTeleporter {

    static PlayerDataManager dataManager;
    static void teleport(ServerPlayerEntity player, MinecraftLocation dest) {
        MinecraftLocation initialPosition = new MinecraftLocation(player);
        player.teleport(player.getServer().getWorld(dest.dim), dest.x, dest.y, dest.z, dest.headYaw, dest.pitch);

    }
    
}