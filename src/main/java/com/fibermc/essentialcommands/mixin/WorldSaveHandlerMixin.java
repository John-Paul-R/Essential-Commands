package com.fibermc.essentialcommands.mixin;

import com.fibermc.essentialcommands.PlayerData;
import com.fibermc.essentialcommands.QueuedTeleport;
import com.fibermc.essentialcommands.access.PlayerEntityAccess;
import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.events.PlayerDeathCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.WorldSaveHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldSaveHandler.class)
public class WorldSaveHandlerMixin {

    @Inject(method = "savePlayerData", at = @At("RETURN"), cancellable = true)
    public void onSavePlayerData(PlayerEntity player, CallbackInfo ci) {
        ((PlayerEntityAccess) player).getEcPlayerData().save();
//        System.out.printf("Saved PlayerData for player: %s\n", player.getName().getString());
    }

}
