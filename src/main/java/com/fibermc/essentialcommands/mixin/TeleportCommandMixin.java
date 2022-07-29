package com.fibermc.essentialcommands.mixin;

import java.util.Set;

import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.types.MinecraftLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.command.TeleportCommand;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

@Mixin(value = TeleportCommand.class)
public class TeleportCommandMixin {

    @Inject(method = "teleport",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;stopRiding()V")
    )
    private static void execute(
        ServerCommandSource source,
        Entity target,
        ServerWorld world,
        double x, double y, double z,
        Set<PlayerPositionLookS2CPacket.Flag> movementFlags,
        float yaw, float pitch,
        @Coerce Object facingLocation,
        CallbackInfo ci
    ) throws CommandSyntaxException {
        // This cast is guaranteed to work because of where we inject.
        var targetPlayer = (ServerPlayerEntity)target;
        var targetPlayerData = ((ServerPlayerEntityAccess)target).ec$getPlayerData();
        targetPlayerData.setPreviousLocation(new MinecraftLocation(targetPlayer));
    }
}
