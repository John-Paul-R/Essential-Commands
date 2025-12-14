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

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.SleepStatus;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;

@Mixin(SleepStatus.class)
public class SleepStatusMixin {

    @Shadow
    private int activePlayers;

    @Inject(
        method = "update",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerPlayer;isSpectator()Z"
        ),
        locals = LocalCapture.CAPTURE_FAILSOFT
    )
    public void orIsAfk(List<ServerPlayer> players,
                        CallbackInfoReturnable<Boolean> cir,
                        int i,
                        int j,
                        Iterator var4,
                        ServerPlayer serverPlayerEntity
    ) {
        if (CONFIG.ENABLE_AFK && CONFIG.AUTO_AFK_ENABLED) {
            var playerData = ((ServerPlayerEntityAccess) serverPlayerEntity).ec$getPlayerData();
            if (!serverPlayerEntity.isSpectator() // mirror check in `update` - don't `--total` unless it was just added to
                && !serverPlayerEntity.isSleeping() // if they're sleeping, toss the custom afk logic
                && playerData.ticksSinceLastActionOrMove() > CONFIG.AUTO_AFK_TICKS
            ) {
                --activePlayers;
            }
        }
    }
}
