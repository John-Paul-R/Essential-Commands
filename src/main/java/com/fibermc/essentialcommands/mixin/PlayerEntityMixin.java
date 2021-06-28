package com.fibermc.essentialcommands.mixin;

import com.fibermc.essentialcommands.EssentialCommands;
import com.fibermc.essentialcommands.PlayerData;
import com.fibermc.essentialcommands.PlayerDataFactory;
import com.fibermc.essentialcommands.QueuedTeleport;
import com.fibermc.essentialcommands.access.PlayerEntityAccess;
import com.fibermc.essentialcommands.events.PlayerDeathCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin implements PlayerEntityAccess {


    @Unique
    public PlayerData ecPlayerData;

    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
    public void onGetDisplayName(CallbackInfoReturnable<Text> cir) {
        try {
            if (this.getEcPlayerData().getNickname() != null) {

                cir.setReturnValue(this.getEcPlayerData().getNickname());
                cir.cancel();
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public PlayerData getEcPlayerData() {
        try {
            return Objects.requireNonNull(ecPlayerData);
        } catch (NullPointerException e) {
            ServerPlayerEntity playerEntity = (ServerPlayerEntity)(Object)this;
            EssentialCommands.LOGGER.warn(String.format("Player data did not exist for player with uuid '%s'. Creating it now.", playerEntity.getUuidAsString()));
            PlayerData playerData = PlayerDataFactory.create(playerEntity);
            setEcPlayerData(playerData);
            return playerData;
        }
    }

    @Override
    public void setEcPlayerData(PlayerData playerData) {
        ecPlayerData = playerData;
    }
}
