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

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.portal.TeleportTransition;

import static com.fibermc.essentialcommands.EssentialCommands.BACKING_CONFIG;
import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;

@SuppressWarnings({"checkstyle:IllegalIdentifierName"})
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends PlayerMixin implements ServerPlayerEntityAccess {

    @Shadow
    public abstract GameType gameMode();

    @Unique
    public QueuedTeleport ecQueuedTeleport;

    @Inject(method = "die", at = @At("HEAD"))
    public void onDeath(DamageSource damageSource, CallbackInfo callbackInfo) {
        PlayerDeathCallback.EVENT.invoker().onDeath(((ServerPlayer) (Object) this), damageSource);
    }

    @Inject(method = "hurtServer", at = @At("RETURN"))
    public void onDamage(ServerLevel world, DamageSource damageSource, float amount, CallbackInfoReturnable<Boolean> cir) {
        // If damage was actually applied...
        if (cir.getReturnValue()) {
            PlayerDamageCallback.EVENT.invoker().onPlayerDamaged(
                ((ServerPlayer) (Object) this),
                damageSource
            );
        }
    }

    @Inject(method = "setGameMode", at = @At("RETURN"))
    public void onChangeGameMode(GameType gameMode, CallbackInfoReturnable<Boolean> cir) {
//        ((ServerPlayerEntityAccess) this).getEcPlayerData().updatePlayer(((ServerPlayerEntity) (Object) this));
    }

    @Inject(method = "teleport(Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/server/level/ServerPlayer;", at = @At(
        value = "INVOKE",
        target = "Lnet/minecraft/server/players/PlayerList;sendAllPlayerInfo(Lnet/minecraft/server/level/ServerPlayer;)V"
    ))
    public void onTeleportBetweenWorlds(TeleportTransition teleportTarget, CallbackInfoReturnable<Entity> cir) {
        var playerData = this.ec$getPlayerData();
        playerData.updatePlayerEntity((ServerPlayer) (Object) this);
    }

    @Inject(method = "triggerDimensionChangeTriggers", at = @At(value = "RETURN"))
    public void onWorldChanged(ServerLevel origin, CallbackInfo ci) {
        var playerData = this.ec$getPlayerData();
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

    @Inject(method = "getTabListDisplayName", at = @At("RETURN"), cancellable = true)
    public void getPlayerListName(CallbackInfoReturnable<Component> cir) {
        if (EssentialCommandsConfig.getValueSafe(BACKING_CONFIG.NICKNAMES_IN_PLAYER_LIST, true)) {
            cir.setReturnValue(((ServerPlayer) (Object) this).getDisplayName());
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

        ServerPlayer playerEntity = (ServerPlayer) (Object) this;
        EssentialCommands.LOGGER.info(String.format(
            "[Essential Commands] Loading PlayerData for player with uuid '%s'.", playerEntity.getStringUUID()));
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

        ServerPlayer playerEntity = (ServerPlayer) (Object) this;
        EssentialCommands.LOGGER.info(String.format(
            "[Essential Commands] Loading PlayerProfile for player with uuid '%s'.", playerEntity.getStringUUID()));
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

        return ec$ecText = ECText.forPlayer((ServerPlayer) (Object) this);
    }

    // Teleport hook (for /back)
    @Inject(method = "teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDLjava/util/Set;FFZ)Z", at = @At("HEAD"))
    public void onTeleport(ServerLevel world, double destX, double destY, double destZ, Set<Relative> flags, float yaw, float pitch, boolean resetCamera, CallbackInfoReturnable<Boolean> cir) {
        if (gameMode() != GameType.SPECTATOR) {
            this.ec$getPlayerData().setPreviousLocation(new MinecraftLocation((ServerPlayer) (Object) this));
        }
    }

    @Inject(method = "onEnterCombat", at = @At("RETURN"))
    public void onEnterCombat(CallbackInfo ci) {
        ec$playerData.setInCombat(true);
    }

    @Inject(method = "onLeaveCombat", at = @At("RETURN"))
    public void onExitCombat(CallbackInfo ci) {
        ec$playerData.setInCombat(false);
    }
}
