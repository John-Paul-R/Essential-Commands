package com.fibermc.essentialcommands.mixin;

import java.util.Optional;

import com.fibermc.essentialcommands.playerdata.PlayerDataManager;
import com.fibermc.essentialcommands.types.MinecraftLocation;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.storage.ValueInput;

@Mixin(targets = "net.minecraft.server.network.config.PrepareSpawnTask$Ready")
public abstract class PrepareSpawnTaskMixin {
    @SuppressWarnings({"checkstyle:MethodName"})
    @Inject(
        method = "spawn",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerPlayer;snapTo(Lnet/minecraft/world/phys/Vec3;FF)V"
        )
    )
    public void onReady_firstConnect_spawnPositionOverride(
        Connection connection, CommonListenerCookie clientData, CallbackInfoReturnable<ServerPlayer> cir,
        @Local(ordinal = 0) ChunkPos chunkPos,
        @Local(ordinal = 0) ServerPlayer serverPlayerEntity,
        @Local(ordinal = 0) Optional<ValueInput> playerNbt
    ) {
        if (playerNbt.isPresent()) {
            // player data existed, definitely isn't first join
            return;
        }

        final MinecraftLocation[] location = new MinecraftLocation[1];
        PlayerDataManager.handleRespawnAtEcSpawn(null, (spawnPos) -> {
            location[0] = spawnPos;
        });

        if (location[0] == null) {
            // EC respawner doesn't want the player on EC spawn
            return;
        }

        var pos = location[0];
        serverPlayerEntity.setServerLevel(serverPlayerEntity.level().getServer().getLevel(location[0].dim()));
        serverPlayerEntity.snapTo(pos.pos(), pos.headYaw(), pos.pitch());
    }
}
