package com.fibermc.essentialcommands.mixin;

import com.fibermc.essentialcommands.PlayerData;
import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.c2s.play.BoatPaddleStateC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {

    private PlayerData getPlayerData() {
        var player= ((ServerPlayNetworkHandler)(Object)this).player;
        return ((ServerPlayerEntityAccess) player).getEcPlayerData();
    }

    private void invokeActEvent(Packet<ServerPlayPacketListener> packet) {
        getPlayerData().PLAYER_ACT_EVENT.invoker().onPlayerAct(packet);
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
}