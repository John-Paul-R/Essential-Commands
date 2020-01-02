package net.fabricmc.essentialcommands.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.fabricmc.essentialcommands.PlayerDataManager;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
 
@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {

    private static PlayerDataManager _dataMgr;

    public static void init(PlayerDataManager dataMgr) {
        _dataMgr = dataMgr;
    }
 
    @Inject(method = "onPlayerConnect", at = @At("HEAD"))
    public void onPlayerConnect(ClientConnection connection, ServerPlayerEntity player, CallbackInfo callbackInfo) {
        
        _dataMgr.onPlayerConnect(connection, player);
    }
}