package com.fibermc.essentialcommands.mixin;

import java.util.Iterator;
import java.util.List;

import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.SleepManager;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;

@Mixin(SleepManager.class)
public class SleepManagerMixin {

    @Shadow
    private int total;

    @Inject(
        method = "update",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/network/ServerPlayerEntity;isSpectator()Z"
        ),
        locals = LocalCapture.CAPTURE_FAILSOFT
    )
    public void orIsAfk(List<ServerPlayerEntity> players,
                        CallbackInfoReturnable<Boolean> cir,
                        int i,
                        int j,
                        Iterator var4,
                        ServerPlayerEntity serverPlayerEntity
    ) {
        if (CONFIG.ENABLE_AFK && CONFIG.AUTO_AFK_ENABLED) {
            var playerData = ((ServerPlayerEntityAccess) serverPlayerEntity).getEcPlayerData();
            if (!serverPlayerEntity.isSpectator() // mirror check in `update` - don't `--total` unless it was just added to
                && !serverPlayerEntity.isSleeping() // if they're sleeping, toss the custom afk logic
                && playerData.ticksSinceLastActionOrMove() > CONFIG.AUTO_AFK_TICKS
            ) {
                --total;
            }
        }
    }
}
