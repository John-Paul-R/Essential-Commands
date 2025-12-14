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

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.commands.TeleportCommand;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Relative;

@Mixin(value = TeleportCommand.class)
public class TeleportCommandMixin {

    @Inject(method = "performTeleport",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDLjava/util/Set;FFZ)Z")
    )
    private static void execute(
        CommandSourceStack source,
        Entity target,
        ServerLevel world,
        double x, double y, double z,
        Set<Relative> movementFlags,
        float yaw, float pitch,
        @Coerce Object facingLocation,
        CallbackInfo ci
    ) throws CommandSyntaxException {
        if (target instanceof ServerPlayer targetPlayer) {
            // This cast is guaranteed to work because of where we inject.
            var targetPlayerData = ((ServerPlayerEntityAccess)target).ec$getPlayerData();
            if (!targetPlayer.isSpectator()) {
                targetPlayerData.setPreviousLocation(new MinecraftLocation(targetPlayer));
            }
        }
    }
}
