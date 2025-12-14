package com.fibermc.essentialcommands.mixin;

import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.playerdata.PlayerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {

    @Unique
    private PlayerData getPlayerData() {
        var player = ((ServerGamePacketListenerImpl) (Object) this).player;
        return ((ServerPlayerEntityAccess) player).ec$getPlayerData();
    }

    @Unique
    private void invokeActEvent(Packet<ServerGamePacketListener> packet) {
        getPlayerData().playerActEvent.invoker().onPlayerAct(packet);
    }

    @Inject(method = "handlePlayerAction", at = @At("RETURN"))
    public void onPlayerAction(ServerboundPlayerActionPacket packet, CallbackInfo ci) {
        invokeActEvent(packet);
    }

    @Inject(method = "handleUseItemOn", at = @At("RETURN"))
    public void onPlayerInteractBlock(ServerboundUseItemOnPacket packet, CallbackInfo ci) {
        invokeActEvent(packet);
    }

    @Inject(method = "handleUseItem", at = @At("RETURN"))
    public void onPlayerInteractItem(ServerboundUseItemPacket packet, CallbackInfo ci) {
        invokeActEvent(packet);
    }

    @Inject(method = "handlePaddleBoat", at = @At("RETURN"))
    public void onBoatPaddleState(ServerboundPaddleBoatPacket packet, CallbackInfo ci) {
        invokeActEvent(packet);
    }

    @Inject(method = "handleInteract", at = @At("RETURN"))
    public void onPlayerInteractEntity(ServerboundInteractPacket packet, CallbackInfo ci) {
        invokeActEvent(packet);
    }
}
