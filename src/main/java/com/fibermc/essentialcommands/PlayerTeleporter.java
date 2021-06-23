package com.fibermc.essentialcommands;

import com.fibermc.essentialcommands.types.MinecraftLocation;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Util;

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
        if (playerHasTpRulesBypass(player, "essentialcommands.bypass.teleport_delay") || Config.TELEPORT_DELAY <= 0) {
            teleport(queuedTeleport.getPlayerData(), queuedTeleport.getDest());
        } else {
            ((ServerPlayerEntityAccess) player).setEcQueuedTeleport(queuedTeleport);
            TeleportRequestManager.getInstance().queueTeleport(queuedTeleport);
            player.sendSystemMessage(
                new LiteralText("Teleporting to ").setStyle(Config.FORMATTING_DEFAULT)
                    .append(new LiteralText(queuedTeleport.getDestName()).setStyle(Config.FORMATTING_ACCENT))
                    .append(new LiteralText(" in ").setStyle(Config.FORMATTING_DEFAULT))
                    .append(new LiteralText(String.format("%.1f", Config.TELEPORT_DELAY)).setStyle(Config.FORMATTING_ACCENT))
                    .append(new LiteralText(" seconds...")).setStyle(Config.FORMATTING_DEFAULT),
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

        // If teleporting between dimensions is disabled and player doesn't have TP rules override
        if (!Config.ALLOW_TELEPORT_BETWEEN_DIMENSIONS && !playerHasTpRulesBypass(player, "essentialcommands.bypass.allow_teleport_between_dimensions")) {
            // If this teleport is between dimensions
            if (dest.dim != player.getServerWorld().getRegistryKey()) {
                player.sendSystemMessage(
                    new LiteralText("Teleport failed. Reason: Interdimensional teleportation disabled.").setStyle(Config.FORMATTING_ERROR),
                    Util.NIL_UUID
                );
                return;
            }
        }

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
            new LiteralText("Teleported to ").setStyle(Config.FORMATTING_DEFAULT)
                .append(dest.toLiteralTextSimple().setStyle(Config.FORMATTING_ACCENT))
                .append(new LiteralText(".").setStyle(Config.FORMATTING_DEFAULT)),
            new UUID(0,0)
        );
    }

    public static boolean playerHasTpRulesBypass(ServerPlayerEntity player, String permission) {
        return (
            (player.hasPermissionLevel(4) && Config.OPS_BYPASS_TELEPORT_RULES)
            || ECPerms.check(player.getCommandSource(), permission, 5)
        );

    }
}