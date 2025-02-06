package com.fibermc.essentialcommands.teleportation;

import com.fibermc.essentialcommands.ECPerms;
import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.playerdata.PlayerData;
import com.fibermc.essentialcommands.types.MinecraftLocation;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import dev.jpcode.eccore.util.TextUtil;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;


import java.util.*;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;
import static net.minecraft.entity.SpawnReason.COMMAND;

public final class PlayerTeleporter {
    private PlayerTeleporter() {}

    public static void requestTeleport(PlayerData pData, MinecraftLocation dest, MutableText destName) {
        requestTeleport(new QueuedLocationTeleport(pData, dest, destName));
    }

    public static void requestTeleport(QueuedTeleport queuedTeleport) {
        ServerPlayerEntity player = queuedTeleport.getPlayerData().getPlayer();
//        if (pData.getTpCooldown() < 0 || player.getServer().getPlayerManager().isOperator(player.getGameProfile())) {
//            //send TP request to tpManager
//        }
        if (playerHasTpRulesBypass(player, ECPerms.Registry.bypass_teleport_delay) || CONFIG.TELEPORT_DELAY_TICKS <= 0) {
            teleport(queuedTeleport.getPlayerData(), queuedTeleport.getDest(), queuedTeleport.getDestName());
        } else {
            TeleportManager.getInstance().queueTeleport(queuedTeleport);
        }
    }

    public static void requestTeleport(ServerPlayerEntity playerEntity, MinecraftLocation dest, MutableText destName) {
        requestTeleport(((ServerPlayerEntityAccess) playerEntity).ec$getPlayerData(), dest, destName);
    }

    public static void teleport(QueuedTeleport queuedTeleport) {
        queuedTeleport.complete();
        teleport(queuedTeleport.getPlayerData(), queuedTeleport.getDest(), queuedTeleport.getDestName());
    }

    public static void teleport(PlayerData pData, MinecraftLocation dest, MutableText destName) { //forceTeleport
        ServerPlayerEntity player = pData.getPlayer();

        // If teleporting between dimensions is disabled and player doesn't have TP rules override
        if (!CONFIG.ALLOW_TELEPORT_BETWEEN_DIMENSIONS
            && !playerHasTpRulesBypass(player, ECPerms.Registry.bypass_allow_teleport_between_dimensions)) {
            // If this teleport is between dimensions
            if (dest.dim() != player.getWorld().getRegistryKey()) {
                pData.sendError("teleport.error.interdimensional_teleport_disabled");
                return;
            }
        }

        execTeleport(player, dest, destName);
    }

    private static void execTeleport(ServerPlayerEntity playerEntity, MinecraftLocation dest, MutableText destName) {
        var playerServer = playerEntity.getServer();
        var targetWorld = playerServer.getWorld(dest.dim());

        if (targetWorld == null) {
            throw new NullPointerException(String.format("Could not find teleport target world, '%s'", dest.dim()));
        }

        BlockPos playerPos = playerEntity.getBlockPos();
        Vec3d targetVec = new Vec3d(dest.pos().x, dest.pos().y, dest.pos().z);

        // **Teleport Player**
        playerEntity.teleport(
            targetWorld,
            dest.pos().x, dest.pos().y, dest.pos().z,
            Set.of(), dest.headYaw(), dest.pitch(),
            false
        );

        // **Check if pet teleportation is enabled**
        if (CONFIG.TELEPORT_FOLLOWERS) {
            double radius = Math.max(CONFIG.TELEPORT_FOLLOWERS_RADIUS, 0); // Ensure radius is not negative
            ServerWorld playerWorld = (ServerWorld) playerEntity.getWorld();

            // Get all tamed pets within radius
            List<TameableEntity> pets = playerWorld.getEntitiesByClass(TameableEntity.class, new Box(playerPos).expand(radius), pet ->
                pet.isTamed() && pet.getOwnerUuid() != null && pet.getOwnerUuid().equals(playerEntity.getUuid()) && !pet.isSitting()
            );

            // **Teleport each pet**
            for (TameableEntity pet : pets) {
                if (pet == null) continue;

                if (pet.getWorld() != targetWorld) {
                    // **Manually re-create entity in the new world**
                    Entity newPet = pet.getType().create(targetWorld, COMMAND);
                    if (newPet instanceof TameableEntity newTamedPet) {
                        newTamedPet.copyPositionAndRotation(pet); // Copy position
                        newTamedPet.setTamed(true, false);
                        newTamedPet.setOwner(playerEntity); // Ensure ownership persists

                        // Remove the old pet & add the new one
                        pet.discard();
                        targetWorld.spawnEntity(newTamedPet);
                        pet = newTamedPet;
                    } else {
                        continue; // Skip if recreation fails
                    }
                }

                // **Ensure chunk is loaded before teleporting pet**
                ((ServerWorld) targetWorld).getChunk((int) targetVec.x >> 4, (int) targetVec.z >> 4);

                pet.teleport(targetVec.x, targetVec.y, targetVec.z, false);
            }
        }

        var playerAccess = ((ServerPlayerEntityAccess) playerEntity);
        var playerProfile = playerAccess.ec$getProfile();
        playerAccess.ec$getPlayerData().sendMessage(
            "teleport.done",
            playerProfile.shouldPrintTeleportCoordinates().orElse(CONFIG.PRINT_TELEPORT_COORDINATES)
                ? TextUtil.join(
                new Text[]{destName, dest.toText(playerProfile)},
                Text.literal(" ")
            )
                : destName
        );
    }


    static boolean playerHasTpRulesBypass(ServerPlayerEntity player, String permission) {
        return (
            (player.hasPermissionLevel(4) && CONFIG.OPS_BYPASS_TELEPORT_RULES)
                || ECPerms.check(player.getCommandSource(), permission, 5)
        );

    }
}
