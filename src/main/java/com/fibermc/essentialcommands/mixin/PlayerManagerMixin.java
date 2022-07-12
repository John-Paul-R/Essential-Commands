package com.fibermc.essentialcommands.mixin;

import com.fibermc.essentialcommands.ECAbilitySources;
import com.fibermc.essentialcommands.events.PlayerConnectCallback;
import com.fibermc.essentialcommands.events.PlayerLeaveCallback;
import com.fibermc.essentialcommands.events.PlayerRespawnCallback;
import io.github.ladysnake.pal.Pal;
import io.github.ladysnake.pal.VanillaAbilities;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Optional;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {

    @Inject(
        method = "onPlayerConnect",
        at = @At(
            value="INVOKE",
            // We inject right after the vanilla player join message is sent. Mostly to ensure LuckPerms permissions are
            // loaded (for role styling in EC MOTD).
            target="Lnet/minecraft/server/PlayerManager;sendToAll(Lnet/minecraft/network/Packet;)V"
        )
    )
    public void onPlayerConnect(ClientConnection connection, ServerPlayerEntity player, CallbackInfo callbackInfo) {
        PlayerConnectCallback.EVENT.invoker().onPlayerConnect(connection, player);
        // Just to be _super_ sure there is no incorrect persistance of this invuln.
        Pal.revokeAbility(player, VanillaAbilities.INVULNERABLE, ECAbilitySources.AFK_INVULN);
    }

    @Inject(method = "remove", at = @At("HEAD"))
    public void onPlayerLeave(ServerPlayerEntity player, CallbackInfo callbackInfo) {
        PlayerLeaveCallback.EVENT.invoker().onPlayerLeave(player);
    }

    @Inject(method = "respawnPlayer", at = @At(
        value = "INVOKE",
//        target = "net.minecraft.server.network.ServerPlayerEntity.copyFrom()V"
        target = "Lnet/minecraft/world/World;getLevelProperties()Lnet/minecraft/world/WorldProperties;"
    ), locals = LocalCapture.CAPTURE_FAILHARD
    )
    public void onRespawnPlayer(ServerPlayerEntity oldServerPlayerEntity, boolean alive, CallbackInfoReturnable<ServerPlayerEntity> cir,
            BlockPos blockPos          ,
            float  f,
            boolean  bl,
            ServerWorld  serverWorld,
            Optional optional2,
            ServerWorld serverWorld2,
            ServerPlayerEntity  serverPlayerEntity,
            boolean  bl2
    ) {
        PlayerRespawnCallback.EVENT.invoker().onPlayerRespawn(oldServerPlayerEntity, serverPlayerEntity);
    }
}