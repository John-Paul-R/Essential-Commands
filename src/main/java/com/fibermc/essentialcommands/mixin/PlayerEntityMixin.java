package com.fibermc.essentialcommands.mixin;

import com.fibermc.essentialcommands.PlayerData;
import com.fibermc.essentialcommands.QueuedTeleport;
import com.fibermc.essentialcommands.access.PlayerEntityAccess;
import com.fibermc.essentialcommands.events.PlayerDeathCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin implements PlayerEntityAccess {


    @Unique
    public PlayerData ecPlayerData;

    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
    public void onGetDisplayName(CallbackInfoReturnable<Text> cir) {
        if (this.getEcPlayerData().getNickname() != null) {
            cir.setReturnValue(this.getEcPlayerData().getNickname());
            cir.cancel();
        }
    }

    @Override
    public PlayerData getEcPlayerData() {
        return ecPlayerData;
    }

    @Override
    public void setEcPlayerData(PlayerData playerData) {
        ecPlayerData = playerData;
    }
}
