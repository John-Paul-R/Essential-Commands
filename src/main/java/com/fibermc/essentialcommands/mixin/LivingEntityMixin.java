package com.fibermc.essentialcommands.mixin;

import com.fibermc.essentialcommands.playerdata.PlayerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(method = "checkBedExists", at = @At("RETURN"), cancellable = true)
    public void modifyIsSleepingInBedReturn(CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity)(Object)this;
        if (!(self instanceof ServerPlayer player)) {
            return;
        }

        var playerData = PlayerData.access(player);
        if (playerData.isSleepingFromCommand()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "stopSleeping", at = @At("RETURN"))
    public void modifyWakeUp(CallbackInfo ci) {
        LivingEntity self = (LivingEntity)(Object)this;
        if (!(self instanceof ServerPlayer player)) {
            return;
        }

        var playerData = PlayerData.access(player);
        if (playerData.isSleepingFromCommand()) {
            playerData.setIsSleepingFromCommand(false);
        }
    }
}

