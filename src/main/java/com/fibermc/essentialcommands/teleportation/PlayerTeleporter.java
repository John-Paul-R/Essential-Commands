package com.fibermc.essentialcommands.teleportation;

import com.fibermc.essentialcommands.ECPerms;
import com.fibermc.essentialcommands.ManagerLocator;
import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.playerdata.PlayerData;
import com.fibermc.essentialcommands.text.ECText;
import com.fibermc.essentialcommands.text.TextFormatType;
import com.fibermc.essentialcommands.types.MinecraftLocation;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;

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
            teleport(queuedTeleport.getPlayerData(), queuedTeleport.getDest());
        } else {
            TeleportManager.getInstance().queueTeleport(queuedTeleport);
        }
    }

    public static void requestTeleport(ServerPlayerEntity playerEntity, MinecraftLocation dest, MutableText destName) {
        requestTeleport(((ServerPlayerEntityAccess) playerEntity).ec$getPlayerData(), dest, destName);
    }

    public static void teleport(QueuedTeleport queuedTeleport) {
        queuedTeleport.complete();
        teleport(queuedTeleport.getPlayerData(), queuedTeleport.getDest());
    }

    public static void teleport(PlayerData pData, MinecraftLocation dest) { //forceTeleport
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

        execTeleport(player, dest);
    }

    public static void teleport(ServerPlayerEntity playerEntity, MinecraftLocation dest) {
        if (ManagerLocator.playerDataEnabled()) {
            teleport(((ServerPlayerEntityAccess) playerEntity).ec$getPlayerData(), dest);
        } else {
            execTeleport(playerEntity, dest);
        }
    }

    private static void execTeleport(ServerPlayerEntity playerEntity, MinecraftLocation dest) {
        playerEntity.teleport(
            playerEntity.getServer().getWorld(dest.dim()),
            dest.pos().x, dest.pos().y, dest.pos().z,
            dest.headYaw(), dest.pitch()
        );

        var playerAccess = ((ServerPlayerEntityAccess) playerEntity);
        var playerProfile = playerAccess.ec$getProfile();
        playerAccess.ec$getPlayerData().sendMessage(
            "teleport.done",
            playerProfile.shouldPrintTeleportCoordinates()
                ? dest.toText(playerProfile)
                : ECText.unstyled("destination").setStyle(playerProfile.getStyle(TextFormatType.Default))
        );
    }

    static boolean playerHasTpRulesBypass(ServerPlayerEntity player, String permission) {
        return (
            (player.hasPermissionLevel(4) && CONFIG.OPS_BYPASS_TELEPORT_RULES)
                || ECPerms.check(player.getCommandSource(), permission, 5)
        );

    }
}
