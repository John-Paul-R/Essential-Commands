package com.fibermc.essentialcommands.mixin;

import java.util.Optional;

import com.fibermc.essentialcommands.ECAbilitySources;
import com.fibermc.essentialcommands.events.PlayerConnectCallback;
import com.fibermc.essentialcommands.events.PlayerLeaveCallback;
import com.fibermc.essentialcommands.events.PlayerRespawnCallback;
import com.fibermc.essentialcommands.playerdata.PlayerDataManager;
import io.github.ladysnake.pal.Pal;
import io.github.ladysnake.pal.VanillaAbilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {

    @Inject(
        method = "onPlayerConnect",
        at = @At(
            value = "INVOKE",
            // We inject right after the vanilla player join message is sent. Mostly to ensure LuckPerms permissions are
            // loaded (for role styling in EC MOTD).
            target = "Lnet/minecraft/server/PlayerManager;sendToAll(Lnet/minecraft/network/packet/Packet;)V"
        )
    )
    public void onPlayerConnect(ClientConnection connection, ServerPlayerEntity player, ConnectedClientData clientData, CallbackInfo ci) {
        PlayerConnectCallback.EVENT.invoker().onPlayerConnect(connection, player);
        // Just to be _super_ sure there is no incorrect persistance of this invuln.
        Pal.revokeAbility(player, VanillaAbilities.INVULNERABLE, ECAbilitySources.AFK_INVULN);
    }

    @Inject(method = "remove", at = @At("HEAD"))
    public void onPlayerLeave(ServerPlayerEntity player, CallbackInfo callbackInfo) {
        PlayerLeaveCallback.EVENT.invoker().onPlayerLeave(player);
    }

    @SuppressWarnings("checkstyle:NoWhitespaceBefore")
    @Inject(method = "respawnPlayer", at = @At(
        value = "INVOKE",
        // This target is near-immediately after the new ServerPlayerEntity is
        // created. This lets us update the EC PlayerData, sooner, might be
        // before the new ServerPlayerEntity is fully initialized.
        target = "Lnet/minecraft/server/network/ServerPlayerEntity;copyFrom(Lnet/minecraft/server/network/ServerPlayerEntity;Z)V"
    ), locals = LocalCapture.CAPTURE_FAILHARD)
    public void onRespawnPlayer(
        ServerPlayerEntity oldServerPlayerEntity, boolean alive, CallbackInfoReturnable<ServerPlayerEntity> cir
        , BlockPos blockPos
        , float f
        , boolean bl
        , ServerWorld serverWorld
        , Optional optional
        , ServerWorld serverWorld2
        , ServerPlayerEntity serverPlayerEntity
    ) {
        PlayerDataManager.handlePlayerDataRespawnSync(oldServerPlayerEntity, serverPlayerEntity);
    }

    @SuppressWarnings({"checkstyle:NoWhitespaceBefore", "checkstyle:MethodName"})
    @Inject(method = "respawnPlayer", at = @At(
        value = "INVOKE",
        // This target lets us modify respawn position
        target = "Lnet/minecraft/server/world/ServerWorld;getLevelProperties()Lnet/minecraft/world/WorldProperties;"
    ), locals = LocalCapture.CAPTURE_FAILHARD)
    public void onRespawnPlayer_afterSetPosition(
        ServerPlayerEntity oldServerPlayerEntity, boolean alive, CallbackInfoReturnable<ServerPlayerEntity> cir
        , BlockPos blockPos
        , float f
        , boolean bl
        , ServerWorld serverWorld
        , Optional optional
        , ServerWorld serverWorld2
        , ServerPlayerEntity serverPlayerEntity
    ) {
        PlayerDataManager.handleRespawnAtEcSpawn(oldServerPlayerEntity, serverPlayerEntity);
        PlayerRespawnCallback.EVENT.invoker().onPlayerRespawn(oldServerPlayerEntity, serverPlayerEntity);
    }
}
