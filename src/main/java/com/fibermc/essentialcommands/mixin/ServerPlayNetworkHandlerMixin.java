package com.fibermc.essentialcommands.mixin;

import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.playerdata.PlayerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.*;
import net.minecraft.server.network.ServerPlayNetworkHandler;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {

    @Unique
    private PlayerData getPlayerData() {
        var player = ((ServerPlayNetworkHandler) (Object) this).player;
        return ((ServerPlayerEntityAccess) player).ec$getPlayerData();
    }

    @Unique
    private void invokeActEvent(Packet<ServerPlayPacketListener> packet) {
        getPlayerData().playerActEvent.invoker().onPlayerAct(packet);
    }

    @Inject(method = "onPlayerAction", at = @At("RETURN"))
    public void onPlayerAction(PlayerActionC2SPacket packet, CallbackInfo ci) {
        invokeActEvent(packet);
    }

    @Inject(method = "onPlayerInteractBlock", at = @At("RETURN"))
    public void onPlayerInteractBlock(PlayerInteractBlockC2SPacket packet, CallbackInfo ci) {
        invokeActEvent(packet);
    }

    @Inject(method = "onPlayerInteractItem", at = @At("RETURN"))
    public void onPlayerInteractItem(PlayerInteractItemC2SPacket packet, CallbackInfo ci) {
        invokeActEvent(packet);
    }

    @Inject(method = "onBoatPaddleState", at = @At("RETURN"))
    public void onBoatPaddleState(BoatPaddleStateC2SPacket packet, CallbackInfo ci) {
        invokeActEvent(packet);
    }

    @Inject(method = "onPlayerInteractEntity", at = @At("RETURN"))
    public void onPlayerInteractEntity(PlayerInteractEntityC2SPacket packet, CallbackInfo ci) {
        invokeActEvent(packet);
    }
}
