package com.fibermc.essentialcommands.mixin;

import java.util.Set;

import com.fibermc.essentialcommands.EssentialCommands;
import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.config.EssentialCommandsConfig;
import com.fibermc.essentialcommands.events.PlayerDamageCallback;
import com.fibermc.essentialcommands.events.PlayerDeathCallback;
import com.fibermc.essentialcommands.playerdata.*;
import com.fibermc.essentialcommands.teleportation.QueuedTeleport;
import com.fibermc.essentialcommands.text.ECText;
import com.fibermc.essentialcommands.types.MinecraftLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;
import net.minecraft.world.TeleportTarget;

import static com.fibermc.essentialcommands.EssentialCommands.BACKING_CONFIG;
import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntityMixin implements ServerPlayerEntityAccess {

    @Shadow
    public abstract boolean isSpectator();

    @Shadow
    public abstract boolean damage(ServerWorld world, DamageSource source, float amount);

    @Unique
    public QueuedTeleport ecQueuedTeleport;

    @Inject(method = "onDeath", at = @At("HEAD"))
    public void onDeath(DamageSource damageSource, CallbackInfo callbackInfo) {
        PlayerDeathCallback.EVENT.invoker().onDeath(((ServerPlayerEntity) (Object) this), damageSource);
    }

    @Inject(method = "damage", at = @At("RETURN"))
    public void onDamage(ServerWorld world, DamageSource damageSource, float amount, CallbackInfoReturnable<Boolean> cir) {
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
//        ((ServerPlayerEntityAccess) this).getEcPlayerData().updatePlayer(((ServerPlayerEntity) (Object) this));
    }

    @Inject(method = "teleportTo(Lnet/minecraft/world/TeleportTarget;)Lnet/minecraft/server/network/ServerPlayerEntity;", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/server/PlayerManager;sendPlayerStatus(Lnet/minecraft/server/network/ServerPlayerEntity;)V"
    ))
    public void onTeleportBetweenWorlds(TeleportTarget teleportTarget, CallbackInfoReturnable<Entity> cir) {
        var playerData = ((ServerPlayerEntityAccess) this).ec$getPlayerData();
        playerData.updatePlayerEntity((ServerPlayerEntity) (Object) this);
    }

    @Inject(method = "worldChanged", at = @At(value = "RETURN"))
    public void onWorldChanged(ServerWorld origin, CallbackInfo ci) {
        var playerData = ((ServerPlayerEntityAccess) this).ec$getPlayerData();
        if (CONFIG.RECHECK_PLAYER_ABILITY_PERMISSIONS_ON_DIMENSION_CHANGE) {
            PlayerDataManager.getInstance().scheduleTask(playerData::clearAbilitiesWithoutPermisisons);
        }
    }

    @Override
    public QueuedTeleport ec$getQueuedTeleport() {
        return ecQueuedTeleport;
    }

    @Override
    public void ec$setQueuedTeleport(QueuedTeleport queuedTeleport) {
        ecQueuedTeleport = queuedTeleport;
    }

    @Override
    public QueuedTeleport ec$endQueuedTeleport() {
        QueuedTeleport prevQueuedTeleport = ecQueuedTeleport;
        ecQueuedTeleport = null;
        return prevQueuedTeleport;
    }

    @Inject(method = "getPlayerListName", at = @At("RETURN"), cancellable = true)
    public void getPlayerListName(CallbackInfoReturnable<Text> cir) {
        if (EssentialCommandsConfig.getValueSafe(BACKING_CONFIG.NICKNAMES_IN_PLAYER_LIST, true)) {
            cir.setReturnValue(((ServerPlayerEntity) (Object) this).getDisplayName());
            cir.cancel();
        }
    }

    @Unique
    public PlayerData ec$playerData;

    @Override
    public PlayerData ec$getPlayerData() {
        if (ec$playerData != null) {
            return ec$playerData;
        }

        ServerPlayerEntity playerEntity = (ServerPlayerEntity) (Object) this;
        EssentialCommands.LOGGER.info(String.format(
            "[Essential Commands] Loading PlayerData for player with uuid '%s'.", playerEntity.getUuidAsString()));
        PlayerData playerData = PlayerDataFactory.create(playerEntity);
        ec$setPlayerData(playerData);
        return playerData;
    }

    @Override
    public void ec$setPlayerData(PlayerData playerData) {
        ec$playerData = playerData;
    }

    @Unique
    public PlayerProfile ec$profile;

    @Override
    public PlayerProfile ec$getProfile() {
        if (ec$profile != null) {
            return ec$profile;
        }

        ServerPlayerEntity playerEntity = (ServerPlayerEntity) (Object) this;
        EssentialCommands.LOGGER.info(String.format(
            "[Essential Commands] Loading PlayerProfile for player with uuid '%s'.", playerEntity.getUuidAsString()));
        PlayerProfile profile = PlayerProfileFactory.create(playerEntity);
        ec$setProfile(profile);
        return profile;
    }

    @Override
    public void ec$setProfile(PlayerProfile profile) {
        ec$profile = profile;
    }

    @Unique
    public ECText ec$ecText;

    @Override
    public ECText ec$getEcText() {
        if (ec$ecText != null) {
            return ec$ecText;
        }

        return ec$ecText = ECText.forPlayer((ServerPlayerEntity) (Object) this);
    }

    // Teleport hook (for /back)
    @Inject(method = "Lnet/minecraft/server/network/ServerPlayerEntity;teleport(Lnet/minecraft/server/world/ServerWorld;DDDLjava/util/Set;FFZ)Z", at = @At("HEAD"))
    public void onTeleport(ServerWorld world, double destX, double destY, double destZ, Set<PositionFlag> flags, float yaw, float pitch, boolean resetCamera, CallbackInfoReturnable<Boolean> cir) {
        if (!isSpectator()) {
            this.ec$getPlayerData().setPreviousLocation(new MinecraftLocation((ServerPlayerEntity) (Object) this));
        }
    }

    @Inject(method = "enterCombat", at = @At("RETURN"))
    public void onEnterCombat(CallbackInfo ci) {
        ec$playerData.setInCombat(true);
    }

    @Inject(method = "endCombat", at = @At("RETURN"))
    public void onExitCombat(CallbackInfo ci) {
        ec$playerData.setInCombat(false);
    }
}
