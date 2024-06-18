package com.fibermc.essentialcommands.mixin;

import java.util.Optional;

import com.fibermc.essentialcommands.ECAbilitySources;
import com.fibermc.essentialcommands.events.PlayerConnectCallback;
import com.fibermc.essentialcommands.events.PlayerLeaveCallback;
import com.fibermc.essentialcommands.events.PlayerRespawnCallback;
import com.fibermc.essentialcommands.playerdata.PlayerDataManager;
import com.fibermc.essentialcommands.types.MinecraftLocation;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import io.github.ladysnake.pal.Pal;
import io.github.ladysnake.pal.VanillaAbilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {

    @Inject(
        method = "onPlayerConnect",
        at = @At(
            value = "INVOKE",
            // We inject right after the vanilla player join message is sent. Mostly to ensure LuckPerms permissions are
            // loaded (for role styling in EC MOTD).
            target = "Lnet/minecraft/server/PlayerManager;sendToAll(Lnet/minecraft/network/packet/Packet;)V"
        )
    )
    public void onPlayerConnect(ClientConnection connection, ServerPlayerEntity player, CallbackInfo callbackInfo) {
        PlayerConnectCallback.EVENT.invoker().onPlayerConnect(connection, player);
        // Just to be _super_ sure there is no incorrect persistance of this invuln.
        Pal.revokeAbility(player, VanillaAbilities.INVULNERABLE, ECAbilitySources.AFK_INVULN);
    }

    @ModifyExpressionValue(
        method = "onPlayerConnect",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/MinecraftServer;getWorld(Lnet/minecraft/registry/RegistryKey;)Lnet/minecraft/server/world/ServerWorld;"
        )
    )
    public ServerWorld onPlayerConnect_firstConnect_spawnPositionOverride(
        ServerWorld original,
        @Local(ordinal = 0, argsOnly = true) ServerPlayerEntity player,
        @Local(ordinal = 0) NbtCompound playerNbt
    ) {
        if (playerNbt != null) {
            // player data existed, definitely isn't first join
            return original;
        }

        MinecraftLocation[] location = new MinecraftLocation[1];
        PlayerDataManager.handleRespawnAtEcSpawn(null, (spawnPos) -> {
            location[0] = spawnPos;
        });

        if (location[0] == null) {
            // EC respawner doesn't want the player on EC spawn
            return original;
        }

        player.setPosition(location[0].pos());
        return original.getServer().getWorld(location[0].dim());
    }

    @Inject(method = "remove", at = @At("HEAD"))
    public void onPlayerLeave(ServerPlayerEntity player, CallbackInfo callbackInfo) {
        PlayerLeaveCallback.EVENT.invoker().onPlayerLeave(player);
    }

    @SuppressWarnings("checkstyle:NoWhitespaceBefore")
    @Inject(method = "respawnPlayer", at = @At(
        value = "INVOKE",
        // This target is near-immediately after the new ServerPlayerEntity is
        // created. This lets us update the EC PlayerData, sooner, might be
        // before the new ServerPlayerEntity is fully initialized.
        target = "Lnet/minecraft/server/network/ServerPlayerEntity;copyFrom(Lnet/minecraft/server/network/ServerPlayerEntity;Z)V"
    ), locals = LocalCapture.CAPTURE_FAILHARD)
    public void onRespawnPlayer(
        ServerPlayerEntity oldServerPlayerEntity, boolean alive, CallbackInfoReturnable<ServerPlayerEntity> cir
        , BlockPos blockPos
        , float f
        , boolean bl
        , ServerWorld serverWorld
        , Optional optional
        , ServerWorld serverWorld2
        , ServerPlayerEntity serverPlayerEntity
    ) {
        PlayerDataManager.handlePlayerDataRespawnSync(oldServerPlayerEntity, serverPlayerEntity);
    }

    @SuppressWarnings({"checkstyle:NoWhitespaceBefore", "checkstyle:MethodName"})
    @Inject(method = "respawnPlayer", at = @At(
        value = "INVOKE",
        // This target lets us modify respawn position and dimension (player maybe not _fully_ initialized, still)
        target = "Lnet/minecraft/server/network/ServerPlayerEntity;<init>(Lnet/minecraft/server/MinecraftServer;Lnet/minecraft/server/world/ServerWorld;Lcom/mojang/authlib/GameProfile;)V"
    ))
    public void onRespawnPlayer_forResawnLocationOverwrite(
        CallbackInfoReturnable<ServerPlayerEntity> cir
        , @Local(ordinal = 0, argsOnly = true) ServerPlayerEntity oldServerPlayerEntity
        , @Local(ordinal = 0) LocalRef<BlockPos> playerSpawnpointBlockPos
        , @Local(ordinal = 0) LocalRef<Optional<Vec3d>> respawnPosition
        , @Local(ordinal = 1) LocalRef<ServerWorld> serverWorld2
    ) {
        PlayerDataManager.handleRespawnAtEcSpawn(oldServerPlayerEntity, (spawnLoc) -> {
            respawnPosition.set(Optional.of(spawnLoc.pos()));
            serverWorld2.set(oldServerPlayerEntity.getServer().getWorld(spawnLoc.dim()));
            playerSpawnpointBlockPos.set(new BlockPos(spawnLoc.intPos()));
        });
    }

    @SuppressWarnings({"checkstyle:NoWhitespaceBefore", "checkstyle:MethodName"})
    @Inject(method = "respawnPlayer", at = @At(
        value = "INVOKE",
        // This target lets us modify respawn position
        target = "Lnet/minecraft/world/World;getLevelProperties()Lnet/minecraft/world/WorldProperties;"
    ))
    public void onRespawnPlayer_afterSetPosition(
        CallbackInfoReturnable<ServerPlayerEntity> cir
        , @Local(ordinal = 0, argsOnly = true) ServerPlayerEntity oldServerPlayerEntity
        , @Local(ordinal = 1) ServerPlayerEntity serverPlayerEntity
    ) {
        PlayerDataManager.handleRespawnAtEcSpawn(oldServerPlayerEntity, (spawnLoc) -> {
            serverPlayerEntity.setServerWorld(serverPlayerEntity.getServer().getWorld(spawnLoc.dim()));
        });
        PlayerRespawnCallback.EVENT.invoker().onPlayerRespawn(oldServerPlayerEntity, serverPlayerEntity);
    }
}
