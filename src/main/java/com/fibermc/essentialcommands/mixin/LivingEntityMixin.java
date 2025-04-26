package com.fibermc.essentialcommands.mixin;

import com.fibermc.essentialcommands.playerdata.PlayerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(method = "isSleepingInBed", at = @At("RETURN"), cancellable = true)
    public void modifyIsSleepingInBedReturn(CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity)(Object)this;
        if (!(self instanceof ServerPlayerEntity player)) {
            return;
        }

        var playerData = PlayerData.access(player);
        if (playerData.isSleepingFromCommand()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "wakeUp", at = @At("RETURN"))
    public void modifyWakeUp(CallbackInfo ci) {
        LivingEntity self = (LivingEntity)(Object)this;
        if (!(self instanceof ServerPlayerEntity player)) {
            return;
        }

        var playerData = PlayerData.access(player);
        if (playerData.isSleepingFromCommand()) {
            playerData.setIsSleepingFromCommand(false);
        }
    }
}

