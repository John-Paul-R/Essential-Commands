package com.fibermc.essentialcommands.teleportation;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.fibermc.essentialcommands.ECPerms;
import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.playerdata.PlayerData;
import com.fibermc.essentialcommands.types.MinecraftLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import dev.jpcode.eccore.util.TextUtil;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;

public final class PlayerTeleporter {
    private static final Logger LOGGER = LogManager.getLogger("PlayerTeleporter");
    private static ProblemReporter errorReporter = new ProblemReporter.Collector();

    private PlayerTeleporter() {}

    public static void requestTeleport(PlayerData pData, MinecraftLocation dest, MutableComponent destName) {
        requestTeleport(new QueuedLocationTeleport(pData, dest, destName));
    }

    public static void requestTeleport(QueuedTeleport queuedTeleport) {
        ServerPlayer player = queuedTeleport.getPlayerData().getPlayer();
//        if (pData.getTpCooldown() < 0 || player.getEntityWorld().getServer().getPlayerManager().isOperator(player.getGameProfile())) {
//            //send TP request to tpManager
//        }
        if (playerHasTpRulesBypass(player, ECPerms.Registry.bypass_teleport_delay) || CONFIG.TELEPORT_DELAY_TICKS <= 0) {
            teleport(queuedTeleport.getPlayerData(), queuedTeleport.getDest(), queuedTeleport.getDestName());
        } else {
            TeleportManager.getInstance().queueTeleport(queuedTeleport);
        }
    }

    public static void requestTeleport(ServerPlayer playerEntity, MinecraftLocation dest, MutableComponent destName) {
        requestTeleport(((ServerPlayerEntityAccess) playerEntity).ec$getPlayerData(), dest, destName);
    }

    public static void teleport(QueuedTeleport queuedTeleport) {
        queuedTeleport.complete();
        teleport(queuedTeleport.getPlayerData(), queuedTeleport.getDest(), queuedTeleport.getDestName());
    }

    public static void teleport(PlayerData pData, MinecraftLocation dest, MutableComponent destName) { //forceTeleport
        ServerPlayer player = pData.getPlayer();

        // If teleporting between dimensions is disabled and player doesn't have TP rules override
        if (!CONFIG.ALLOW_TELEPORT_BETWEEN_DIMENSIONS
            && !playerHasTpRulesBypass(player, ECPerms.Registry.bypass_allow_teleport_between_dimensions)) {
            // If this teleport is between dimensions
            if (dest.dim() != player.level().dimension()) {
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
    private static void execTeleport(ServerPlayer playerEntity, MinecraftLocation dest, MutableComponent destName) {
        var playerServer = playerEntity.level().getServer();
        var targetWorld = playerServer.getLevel(dest.dim());

        if (targetWorld == null) {
            throw new NullPointerException(String.format("Could not find teleport target world, '%s'", dest.dim()));
        }

        BlockPos playerPos = playerEntity.blockPosition();
        Vec3 targetVec = new Vec3(dest.pos().x, dest.pos().y, dest.pos().z);

        playerEntity.teleportTo(targetWorld, targetVec.x, targetVec.y, targetVec.z, Set.of(), dest.headYaw(), dest.pitch(), false);

        if (CONFIG.TELEPORT_FOLLOWERS) {
            List<TamableAnimal> pets = detectTamedPets(playerEntity, playerPos);
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
    private static List<TamableAnimal> detectTamedPets(ServerPlayer playerEntity, BlockPos playerPos) {
        double radius = Math.max(CONFIG.TELEPORT_FOLLOWERS_RADIUS, 0);
        ServerLevel playerWorld = (ServerLevel) playerEntity.level();

        return playerWorld.getEntitiesOfClass(TamableAnimal.class, new AABB(playerPos).inflate(radius), pet -> {
            boolean isTamed = pet.isTame();
            var petOwner = pet.getOwner();
            if (petOwner == null) {
                LOGGER.warn("failed to find owner for pet with id '{}', name '{}'", pet.getUUID(), pet.getDisplayName().getString());
                return false;
            }
            UUID ownerUuid = petOwner.getUUID();
            boolean isSameOwner = ownerUuid != null && ownerUuid.equals(playerEntity.getUUID());
            boolean isSitting = pet.isOrderedToSit();

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
    private static void teleportTamedEntities(List<TamableAnimal> pets, ServerLevel targetWorld, Vec3 targetVec, ServerPlayer playerEntity) {
        for (TamableAnimal pet : pets) {
            if (pet.level() != targetWorld) {
                if (!transferEntityToWorld(pet, targetWorld, targetVec, playerEntity)) {
                    LOGGER.warn("Failed to transfer pet {} ({}) to {}", pet.getType().getDescriptionId(), pet.getUUID(), targetWorld.dimension().identifier());
                }
            } else {
                targetWorld.getChunk((int) targetVec.x >> 4, (int) targetVec.z >> 4);
                pet.randomTeleport(targetVec.x, targetVec.y + 0.5, targetVec.z, false);
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
    private static boolean transferEntityToWorld(TamableAnimal pet, ServerLevel targetWorld, Vec3 targetVec, ServerPlayer playerEntity) {
        TagValueOutput entityData = TagValueOutput.createWithoutContext(errorReporter);
//        entityData.
        pet.saveAsPassenger(entityData); // Store full entity data

        var entityDataReadView = TagValueInput.create(errorReporter, targetWorld.registryAccess(), entityData.buildResult());
        Entity newPet = EntityType.loadEntityRecursive(entityDataReadView, targetWorld, EntitySpawnReason.COMMAND, (e) -> {
            e.setPosRaw(targetVec.x, targetVec.y, targetVec.z);
            return e;
        });

        if (newPet instanceof TamableAnimal newTamedPet) {
            newTamedPet.setTame(true, true);
            newTamedPet.setOwner(playerEntity);
            targetWorld.addFreshEntity(newTamedPet);

            // sanity check to make sure the entity has spawned
            if (newTamedPet.isRemoved()) {
                LOGGER.error("Failed to spawn pet {} ({}) in {}", newTamedPet.getType().getDescriptionId(), newTamedPet.getUUID(), targetWorld.dimension().identifier());
                return false;
            }

            pet.discard();
            return true;
        } else {
            // Failed to create entity from NBT
            LOGGER.error("Failed to create entity from NBT for pet ({})!", pet.getUUID());
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
    private static void sendTeleportMessage(ServerPlayer playerEntity, MutableComponent destName, MinecraftLocation dest) {
        var playerAccess = ((ServerPlayerEntityAccess) playerEntity);
        var playerProfile = playerAccess.ec$getProfile();
        playerAccess.ec$getPlayerData().sendMessage(
            "teleport.done",
            playerProfile.shouldPrintTeleportCoordinates().orElse(CONFIG.PRINT_TELEPORT_COORDINATES)
                ? TextUtil.join(
                new Component[]{destName, dest.toText(playerProfile)},
                Component.literal(" ")
            )
                : destName
        );
    }

    static boolean playerHasTpRulesBypass(ServerPlayer player, String permission) {
        return (
            (player.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.OWNERS)) && CONFIG.OPS_BYPASS_TELEPORT_RULES)
                || ECPerms.check(player.createCommandSourceStack(), permission, 5)
        );
    }
}
