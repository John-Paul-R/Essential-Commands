package com.fibermc.essentialcommands.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;

public interface PlayerDamageCallback {
    Event<PlayerDamageCallback> EVENT = EventFactory.createArrayBacked(PlayerDamageCallback.class,
        (listeners) -> (playerId, damageSource) -> {
            for (PlayerDamageCallback event : listeners) {
                event.onPlayerDamaged(playerId, damageSource);
            }
        });

    void onPlayerDamaged(ServerPlayerEntity playerID, DamageSource damageSource);
}
