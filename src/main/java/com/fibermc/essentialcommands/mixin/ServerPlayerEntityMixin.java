package com.fibermc.essentialcommands.mixin;

import com.fibermc.essentialcommands.EssentialCommands;
import com.fibermc.essentialcommands.PlayerData;
import com.fibermc.essentialcommands.PlayerDataFactory;
import com.fibermc.essentialcommands.QueuedTeleport;
import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.events.PlayerDamageCallback;
import com.fibermc.essentialcommands.events.PlayerDeathCallback;
import com.fibermc.essentialcommands.types.MinecraftLocation;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;
import net.minecraft.world.WorldProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Objects;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;
import static com.fibermc.essentialcommands.EssentialCommands.LOGGER;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin extends PlayerEntityMixin implements ServerPlayerEntityAccess {

    @Unique
    public QueuedTeleport ecQueuedTeleport;

    @Inject(method = "onDeath", at = @At("HEAD"))
    public void onDeath(DamageSource damageSource, CallbackInfo callbackInfo) {
        PlayerDeathCallback.EVENT.invoker().onDeath(((ServerPlayerEntity) (Object) this), damageSource);
    }

    @Inject(method = "damage", at = @At("RETURN"))
    public void onDamage(DamageSource damageSource, float amount, CallbackInfoReturnable<Boolean> cir) {
        // If damage was actually applied...
        if (cir.getReturnValue()) {
            PlayerDamageCallback.EVENT.invoker().onPlayerDamaged(
                ((ServerPlayerEntity) (Object) this),
                damageSource
            );
        }
    }

    @Inject(method = "changeGameMode", at = @At("RETURN"))
    public void onChangeGameMode(GameMode gameMode, CallbackInfoReturnable<Boolean> cir) {
        ((ServerPlayerEntityAccess) this).getEcPlayerData().updateFlight();
    }

    @Inject(method = "teleport", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/server/PlayerManager;sendPlayerStatus(Lnet/minecraft/server/network/ServerPlayerEntity;)V"
    ), locals = LocalCapture.CAPTURE_FAILSOFT)
    public void onTeleportBetweenWorlds(ServerWorld targetWorld, double x, double y, double z, float yaw, float pitch, CallbackInfo ci,
                                        ServerWorld  serverWorld,
                                        WorldProperties worldProperties
    ) {
        ((ServerPlayerEntityAccess) this).getEcPlayerData().updatePlayer((ServerPlayerEntity) (Object) this);
    }

    @Override
    public QueuedTeleport getEcQueuedTeleport() {
        return ecQueuedTeleport;
    }

    @Override
    public void setEcQueuedTeleport(QueuedTeleport queuedTeleport) {
        ecQueuedTeleport = queuedTeleport;
    }

    @Override
    public QueuedTeleport endEcQueuedTeleport() {
        QueuedTeleport prevQueuedTeleport = ecQueuedTeleport;
        ecQueuedTeleport = null;
        return prevQueuedTeleport;
    }

    @Inject(method = "getPlayerListName", at = @At("RETURN"), cancellable = true)
    public void getPlayerListName(CallbackInfoReturnable<Text> cir) {
        if (CONFIG.NICKNAMES_IN_PLAYER_LIST.getValue()) {
            cir.setReturnValue(((ServerPlayerEntity)(Object)this).getDisplayName());
            cir.cancel();
        }
    }

    @Unique
    public PlayerData ecPlayerData;

    @Override//method = "getDisplayName", at = @At("RETURN"), cancellable = true
    public void onGetDisplayName(CallbackInfoReturnable<Text> cir) {
        try {
            if (this.getEcPlayerData().getNickname() != null) {
                MutableText nickname = this.getEcPlayerData().getFullNickname();
                // Re-add "whisper" click event unless the nickname has a click event set.
                Style nicknameStyle = nickname.getStyle();
                if (nicknameStyle.getClickEvent() == null) {
                    nickname.setStyle(nicknameStyle.withClickEvent(cir.getReturnValue().getStyle().getClickEvent()));
                }
                // Send nickname (styled appropriately for player team) as return value for getDisplayName().
                ServerPlayerEntity serverPlayerEntity = ((ServerPlayerEntity)(Object)this);
                cir.setReturnValue(Team.decorateName(
                        serverPlayerEntity.getScoreboard().getPlayerTeam(serverPlayerEntity.getEntityName()),
                        nickname
                ));
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

    // Teleport hook (for /back)
    @Inject(method = "teleport", at = @At("HEAD"))
    public void onTeleport(ServerWorld targetWorld, double x, double y, double z, float yaw, float pitch, CallbackInfo ci) {
        this.getEcPlayerData().setPreviousLocation(new MinecraftLocation((ServerPlayerEntity)(Object)this));
    }
}
