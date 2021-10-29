package com.fibermc.essentialcommands;

import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.types.MinecraftLocation;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.util.Util;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;

/**
 * Teleporter
 */
public class PlayerTeleporter {

    public static void requestTeleport(PlayerData pData, MinecraftLocation dest, MutableText destName) {
        requestTeleport(new QueuedLocationTeleport(pData, dest, destName));
    }
    public static void requestTeleport(QueuedTeleport queuedTeleport) {
        ServerPlayerEntity player = queuedTeleport.getPlayerData().getPlayer();
//        if (pData.getTpCooldown() < 0 || player.getMinecraftServer().getPlayerManager().isOperator(player.getGameProfile())) {
//            //send TP request to tpManager
//        }
        if (playerHasTpRulesBypass(player, ECPerms.Registry.bypass_teleport_delay) || CONFIG.TELEPORT_DELAY.getValue() <= 0) {
            teleport(queuedTeleport.getPlayerData(), queuedTeleport.getDest());
        } else {
            ((ServerPlayerEntityAccess) player).setEcQueuedTeleport(queuedTeleport);
            TeleportRequestManager.getInstance().queueTeleport(queuedTeleport);
            player.sendSystemMessage(
                ECText.getInstance().getText("teleport.queued.1").setStyle(CONFIG.FORMATTING_DEFAULT.getValue())
                    .append(queuedTeleport.getDestName().setStyle(CONFIG.FORMATTING_ACCENT.getValue()))
                    .append(ECText.getInstance().getText("teleport.queued.2").setStyle(CONFIG.FORMATTING_DEFAULT.getValue()))
                    .append(new LiteralText(String.format("%.1f", CONFIG.TELEPORT_DELAY.getValue())).setStyle(CONFIG.FORMATTING_ACCENT.getValue()))
                    .append(ECText.getInstance().getText("teleport.queued.3")).setStyle(CONFIG.FORMATTING_DEFAULT.getValue()),
                Util.NIL_UUID
            );
        }
    }
    public static void requestTeleport(ServerPlayerEntity playerEntity, MinecraftLocation dest, MutableText destName) {
        requestTeleport(((ServerPlayerEntityAccess)playerEntity).getEcPlayerData(), dest, destName);
    }
    public static void teleport(QueuedTeleport queuedTeleport) {
        queuedTeleport.complete();
        teleport(queuedTeleport.getPlayerData(), queuedTeleport.getDest());
    }
    public static void teleport(PlayerData pData, MinecraftLocation dest) {//forceTeleport
        ServerPlayerEntity player = pData.getPlayer();

        // If teleporting between dimensions is disabled and player doesn't have TP rules override
        if (!CONFIG.ALLOW_TELEPORT_BETWEEN_DIMENSIONS.getValue() && !playerHasTpRulesBypass(player, ECPerms.Registry.bypass_allow_teleport_between_dimensions)) {
            // If this teleport is between dimensions
            if (dest.dim != player.getServerWorld().getRegistryKey()) {
                player.sendSystemMessage(
                    ECText.getInstance().getText("teleport.error.interdimensional_teleport_disabled").setStyle(CONFIG.FORMATTING_ERROR.getValue()),
                    Util.NIL_UUID
                );
                return;
            }
        }

        execTeleport(player, dest);
    }
    public static void teleport(ServerPlayerEntity playerEntity, MinecraftLocation dest) {
        if (ManagerLocator.playerDataEnabled())
            teleport(((ServerPlayerEntityAccess)playerEntity).getEcPlayerData(), dest);
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
            ECText.getInstance().getText("teleport.done.1").setStyle(CONFIG.FORMATTING_DEFAULT.getValue())
                .append(dest.toLiteralTextSimple().setStyle(CONFIG.FORMATTING_ACCENT.getValue()))
                .append(new LiteralText(".").setStyle(CONFIG.FORMATTING_DEFAULT.getValue())),
            Util.NIL_UUID
        );
    }

    public static boolean playerHasTpRulesBypass(ServerPlayerEntity player, String permission) {
        return (
            (player.hasPermissionLevel(4) && CONFIG.OPS_BYPASS_TELEPORT_RULES.getValue())
            || ECPerms.check(player.getCommandSource(), permission, 5)
        );

    }
}