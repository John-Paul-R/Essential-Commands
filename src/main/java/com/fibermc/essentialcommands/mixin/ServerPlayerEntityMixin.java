package com.fibermc.essentialcommands.mixin;

import com.fibermc.essentialcommands.events.PlayerDamageCallback;
import com.fibermc.essentialcommands.events.PlayerDeathCallback;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {

    public ServerPlayerEntityMixin(World world, BlockPos blockPos, float yaw, GameProfile profile) {
        super(world, blockPos, yaw, profile);
    }

    @Inject(method = "onDeath", at = @At("HEAD"))
    public void onDeath(DamageSource damageSource, CallbackInfo callbackInfo) {
        PlayerDeathCallback.EVENT.invoker().onDeath(super.getGameProfile().getId(), damageSource);
    }

    @Inject(method = "damage", at = @At("RETURN"))
    public void onDamage(DamageSource damageSource, float amount, CallbackInfoReturnable<Boolean> cir) {
        // If damage was actually applied...
        if (cir.getReturnValue()) {
            PlayerDamageCallback.EVENT.invoker().onPlayerDamaged(
                super.getGameProfile().getId(),
                damageSource
            );
        }
    }
}
