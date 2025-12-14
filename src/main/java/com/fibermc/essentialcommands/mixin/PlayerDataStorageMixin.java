package com.fibermc.essentialcommands.mixin;

import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.PlayerDataStorage;

@Mixin(PlayerDataStorage.class)
public class PlayerDataStorageMixin {

    @Inject(method = "save", at = @At("RETURN"))
    public void onSavePlayerData(Player player, CallbackInfo ci) {
        ((ServerPlayerEntityAccess) player).ec$getPlayerData().save();
//        System.out.printf("Saved PlayerData for player: %s\n", player.getName().getString());
    }

}
