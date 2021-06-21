package com.fibermc.essentialcommands.events;

import com.fibermc.essentialcommands.mixin.ServerPlayerEntityMixin;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.UUID;

public interface PlayerDamageCallback {
    Event<PlayerDamageCallback> EVENT = EventFactory.createArrayBacked(PlayerDamageCallback.class,
        (listeners) -> (playerId, damageSource) -> {
            for (PlayerDamageCallback event : listeners) {
                event.onPlayerDamaged(playerId, damageSource);
            }
        });

    void onPlayerDamaged(ServerPlayerEntity playerID, DamageSource damageSource);
}
