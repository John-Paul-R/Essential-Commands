package com.fibermc.essentialcommands.events;

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

    void onPlayerDamaged(UUID playerID, DamageSource damageSource);
}
