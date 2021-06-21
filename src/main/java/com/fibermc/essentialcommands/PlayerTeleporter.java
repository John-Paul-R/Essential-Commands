package com.fibermc.essentialcommands;

import com.fibermc.essentialcommands.types.MinecraftLocation;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

import java.util.UUID;

/**
 * Teleporter
 */
public class PlayerTeleporter {

    public static void requestTeleport(PlayerData pData, MinecraftLocation dest, String destName) {
        requestTeleport(new QueuedLocationTeleport(pData, dest, destName));
    }
    public static void requestTeleport(QueuedTeleport queuedTeleport) {
        ServerPlayerEntity player = queuedTeleport.getPlayerData().getPlayer();
//        if (pData.getTpCooldown() < 0 || player.getServer().getPlayerManager().isOperator(player.getGameProfile())) {
//            //send TP request to tpManager
//        }
        if (player.hasPermissionLevel(4) || Config.TELEPORT_DELAY <= 0) {
            teleport(queuedTeleport.getPlayerData(), queuedTeleport.getDest());
        } else {
            ((ServerPlayerEntityAccess) player).setEcQueuedTeleport(queuedTeleport);
            TeleportRequestManager.getInstance().queueTeleport(queuedTeleport);
            player.sendSystemMessage(
                new LiteralText("Teleporting to ").formatted(Config.FORMATTING_DEFAULT)
                    .append(new LiteralText(queuedTeleport.getDestName()).formatted(Config.FORMATTING_ACCENT))
                    .append(new LiteralText(" in ").formatted(Config.FORMATTING_DEFAULT))
                    .append(new LiteralText(String.format("%.1f", Config.TELEPORT_DELAY)).formatted(Config.FORMATTING_ACCENT))
                    .append(new LiteralText(" seconds...")).formatted(Config.FORMATTING_DEFAULT),
                new UUID(0,0)
            );
        }
    }
    public static void requestTeleport(ServerPlayerEntity playerEntity, MinecraftLocation dest, String destName) {
        requestTeleport(PlayerDataManager.getInstance().getOrCreate(playerEntity), dest, destName);
    }
    public static void teleport(QueuedTeleport queuedTeleport) {
        queuedTeleport.complete();
        teleport(queuedTeleport.getPlayerData(), queuedTeleport.getDest());
    }
    public static void teleport(PlayerData pData, MinecraftLocation dest) {//forceTeleport
        ServerPlayerEntity player = pData.getPlayer();
        pData.setPreviousLocation(new MinecraftLocation(player));

        execTeleport(player, dest);
    }
    public static void teleport(ServerPlayerEntity playerEntity, MinecraftLocation dest) {
        if (ManagerLocator.playerDataEnabled())
            teleport(PlayerDataManager.getInstance().getOrCreate(playerEntity), dest);
        else
            execTeleport(playerEntity, dest);
    }

    private static void execTeleport(ServerPlayerEntity playerEntity, MinecraftLocation dest) {
        playerEntity.teleport(
            playerEntity.getServer().getWorld(dest.dim),
            dest.pos.x, dest.pos.y, dest.pos.z,
            dest.headYaw, dest.pitch
        );
        playerEntity.sendSystemMessage(
            new LiteralText("Teleported to ").formatted(Config.FORMATTING_DEFAULT)
                .append(dest.toLiteralTextSimple().formatted(Config.FORMATTING_ACCENT))
                .append(new LiteralText(".").formatted(Config.FORMATTING_DEFAULT)),
            new UUID(0,0)
        );
    }
}