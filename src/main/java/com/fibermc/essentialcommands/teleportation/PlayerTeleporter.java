package com.fibermc.essentialcommands.teleportation;

import com.fibermc.essentialcommands.ECPerms;
import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.playerdata.PlayerData;
import com.fibermc.essentialcommands.types.MinecraftLocation;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import dev.jpcode.eccore.util.TextUtil;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.util.*;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;

public final class PlayerTeleporter {
    private static final Logger LOGGER = LogManager.getLogger("PlayerTeleporter");
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

    /**
     * Executes the teleportation of a player and their tamed pets to a specified destination.
     *
     * @param playerEntity the player entity to be teleported
     * @param dest the destination location for the teleportation
     * @param destName the name of the destination to be displayed in messages
     */
    private static void execTeleport(ServerPlayerEntity playerEntity, MinecraftLocation dest, MutableText destName) {
        var playerServer = playerEntity.getServer();
        var targetWorld = playerServer.getWorld(dest.dim());

        if (targetWorld == null) {
            throw new NullPointerException(String.format("Could not find teleport target world, '%s'", dest.dim()));
        }

        BlockPos playerPos = playerEntity.getBlockPos();
        Vec3d targetVec = new Vec3d(dest.pos().x, dest.pos().y, dest.pos().z);

        playerEntity.teleport(targetWorld, targetVec.x, targetVec.y, targetVec.z, Set.of(), dest.headYaw(), dest.pitch(), false);

        if (CONFIG.TELEPORT_FOLLOWERS) {
            List<TameableEntity> pets = detectTamedPets(playerEntity, playerPos);
            teleportTamedEntities(pets, targetWorld, targetVec, playerEntity);
        }

        sendTeleportMessage(playerEntity, destName, dest);
    }

    /**
     * Detects tamed pets within a specified radius around the player's position.
     *
     * @param playerEntity the player entity whose pets are being detected
     * @param playerPos the position of the player
     * @return a list of tamed pets that belong to the player and are not sitting
     */
    private static List<TameableEntity> detectTamedPets(ServerPlayerEntity playerEntity, BlockPos playerPos) {
        double radius = Math.max(CONFIG.TELEPORT_FOLLOWERS_RADIUS, 0);
        ServerWorld playerWorld = (ServerWorld) playerEntity.getWorld();

        return playerWorld.getEntitiesByClass(TameableEntity.class, new Box(playerPos).expand(radius), pet -> {
            boolean isTamed = pet.isTamed();
            UUID ownerUuid = pet.getOwnerUuid();
            boolean isSameOwner = ownerUuid != null && ownerUuid.equals(playerEntity.getUuid());
            boolean isSitting = pet.isSitting();

            return isTamed && isSameOwner && !isSitting;
        });
    }

    /**
     * Teleports a list of tamed entities to a specified position in a target world.
     *
     * @param pets the list of tamed entities to be teleported
     * @param targetWorld the world where the entities will be teleported
     * @param targetVec the position where the entities will be teleported
     * @param playerEntity the player entity who owns the tamed entities
     */
    private static void teleportTamedEntities(List<TameableEntity> pets, ServerWorld targetWorld, Vec3d targetVec, ServerPlayerEntity playerEntity) {
        for (TameableEntity pet : pets) {
            if (pet.getWorld() != targetWorld) {
                if (!transferEntityToWorld(pet, targetWorld, targetVec, playerEntity)) {
                    LOGGER.warn("Failed to transfer pet {} ({}) to {}", pet.getType().getTranslationKey(), pet.getUuid(), targetWorld.getRegistryKey().getValue());
                }
            } else {
                targetWorld.getChunk((int) targetVec.x >> 4, (int) targetVec.z >> 4);
                pet.teleport(targetVec.x, targetVec.y + 0.5, targetVec.z, false);
            }
        }
    }

    /**
     * Transfers a tamed entity to a specified target world and position.
     *
     * @param pet the tamed entity to be transferred
     * @param targetWorld the world where the entity will be transferred
     * @param targetVec the position where the entity will be transferred
     * @param playerEntity the player entity who owns the tamed entity
     * @return true if the entity was successfully transferred, false otherwise
     */
    private static boolean transferEntityToWorld(TameableEntity pet, ServerWorld targetWorld, Vec3d targetVec, ServerPlayerEntity playerEntity) {
        NbtCompound entityData = new NbtCompound();
        pet.saveSelfNbt(entityData); // Store full entity data

        Entity newPet = EntityType.loadEntityWithPassengers(entityData, targetWorld, SpawnReason.COMMAND, (e) -> {
            e.setPos(targetVec.x, targetVec.y, targetVec.z);
            return e;
        });

        if (newPet instanceof TameableEntity newTamedPet) {
            newTamedPet.setTamed(true, true);
            newTamedPet.setOwner(playerEntity);
            targetWorld.spawnEntity(newTamedPet);

            // sanity check to make sure the entity has spawned
            if (newTamedPet.isRemoved()) {
                LOGGER.error("Failed to spawn pet {} ({}) in {}", newTamedPet.getType().getTranslationKey(), newTamedPet.getUuid(), targetWorld.getRegistryKey().getValue());
                return false;
            }

            pet.discard();
            return true;
        } else {
            // Failed to create entity from NBT
            LOGGER.error("Failed to create entity from NBT for pet ({})!", pet.getUuid());
            return false;
        }
    }

    /**
     * Sends a teleportation message to the player.
     *
     * @param playerEntity the player entity to whom the message will be sent
     * @param destName the name of the destination to be displayed in the message
     * @param dest the destination location for the teleportation
     */
    private static void sendTeleportMessage(ServerPlayerEntity playerEntity, MutableText destName, MinecraftLocation dest) {
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
