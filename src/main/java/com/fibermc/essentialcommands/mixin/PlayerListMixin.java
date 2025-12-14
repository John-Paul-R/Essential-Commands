package com.fibermc.essentialcommands.mixin;

import com.fibermc.essentialcommands.ECAbilitySources;
import com.fibermc.essentialcommands.events.PlayerConnectCallback;
import com.fibermc.essentialcommands.events.PlayerLeaveCallback;
import com.fibermc.essentialcommands.events.PlayerRespawnCallback;
import com.fibermc.essentialcommands.playerdata.PlayerDataManager;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import io.github.ladysnake.pal.Pal;
import io.github.ladysnake.pal.VanillaAbilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {

    @Inject(
        method = "placeNewPlayer",
        at = @At(
            value = "INVOKE",
            // We inject right after the vanilla player join message is sent. Mostly to ensure LuckPerms permissions are
            // loaded (for role styling in EC MOTD).
            target = "Lnet/minecraft/server/players/PlayerList;broadcastAll(Lnet/minecraft/network/protocol/Packet;)V"
        )
    )
    public void onPlayerConnect(Connection connection, ServerPlayer player, CommonListenerCookie clientData, CallbackInfo ci) {
        PlayerConnectCallback.EVENT.invoker().onPlayerConnect(connection, player);
        // Just to be _super_ sure there is no incorrect persistance of this invuln.
        Pal.revokeAbility(player, VanillaAbilities.INVULNERABLE, ECAbilitySources.AFK_INVULN);
    }

    @Inject(method = "remove", at = @At("HEAD"))
    public void onPlayerLeave(ServerPlayer player, CallbackInfo callbackInfo) {
        PlayerLeaveCallback.EVENT.invoker().onPlayerLeave(player);
    }

    @SuppressWarnings("checkstyle:NoWhitespaceBefore")
    @Inject(method = "respawn", at = @At(
        value = "INVOKE",
        // This target is near-immediately after the new ServerPlayerEntity is
        // created. This lets us update the EC PlayerData, sooner, might be
        // before the new ServerPlayerEntity is fully initialized.
        target = "Lnet/minecraft/server/level/ServerPlayer;restoreFrom(Lnet/minecraft/server/level/ServerPlayer;Z)V"
    ))
    public void onRespawnPlayer(
        ServerPlayer oldServerPlayerEntity, boolean alive, Entity.RemovalReason removalReason,
        CallbackInfoReturnable<ServerPlayer> cir,
        @Local(ordinal = 1) ServerPlayer serverPlayerEntity
    ) {
        PlayerDataManager.handlePlayerDataRespawnSync(oldServerPlayerEntity, serverPlayerEntity);
    }

    @SuppressWarnings({"checkstyle:NoWhitespaceBefore", "checkstyle:MethodName", "checkstyle:LineLength"})
    @Inject(method = "respawn", at = @At(
        value = "INVOKE",
        // This target lets us modify respawn position and dimension (player maybe not _fully_ initialized, still)
        target = "Lnet/minecraft/server/level/ServerPlayer;<init>(Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/server/level/ServerLevel;Lcom/mojang/authlib/GameProfile;Lnet/minecraft/server/level/ClientInformation;)V"
    ))
    public void onRespawnPlayer_forRespawnLocationOverwrite(
        CallbackInfoReturnable<ServerPlayer> cir
        , @Local(ordinal = 0, argsOnly = true) ServerPlayer oldServerPlayerEntity
        , @Local(ordinal = 0) LocalRef<TeleportTransition> teleportTargetLocalRef
        , @Local(ordinal = 0) LocalRef<ServerLevel> teleportTargetServerWorld
    ) {
        PlayerDataManager.handleRespawnAtEcSpawn(oldServerPlayerEntity, (spawnLoc) -> {
            var targetWorld = oldServerPlayerEntity.level().getServer().getLevel(spawnLoc.dim());
            teleportTargetServerWorld.set(targetWorld);
            teleportTargetLocalRef.set(new TeleportTransition(
                targetWorld,
                spawnLoc.pos(),
                Vec3.ZERO,
                0,
                0,
                TeleportTransition.DO_NOTHING
            ));
        });
    }

    @SuppressWarnings({"checkstyle:NoWhitespaceBefore", "checkstyle:MethodName"})
    @Inject(method = "respawn", at = @At(
        value = "INVOKE",
        // This target lets us modify respawn position
        target = "Lnet/minecraft/server/level/ServerLevel;getLevelData()Lnet/minecraft/world/level/storage/LevelData;"
    ))
    public void onRespawnPlayer_afterSetPosition(
        CallbackInfoReturnable<ServerPlayer> cir
        , @Local(ordinal = 0, argsOnly = true) ServerPlayer oldServerPlayerEntity
        , @Local(ordinal = 1) ServerPlayer serverPlayerEntity
    ) {
        PlayerDataManager.handleRespawnAtEcSpawn(oldServerPlayerEntity, (spawnLoc) -> {
            serverPlayerEntity.setServerLevel(serverPlayerEntity.level().getServer().getLevel(spawnLoc.dim()));
        });
        PlayerRespawnCallback.EVENT.invoker().onPlayerRespawn(oldServerPlayerEntity, serverPlayerEntity);
    }
}
