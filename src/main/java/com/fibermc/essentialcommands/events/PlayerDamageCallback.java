package com.fibermc.essentialcommands.events;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface PlayerDamageCallback {
    Event<PlayerDamageCallback> EVENT = EventFactory.createArrayBacked(PlayerDamageCallback.class,
        (listeners) -> (playerId, damageSource) -> {
            for (PlayerDamageCallback event : listeners) {
                event.onPlayerDamaged(playerId, damageSource);
            }
        });

    void onPlayerDamaged(ServerPlayer playerID, DamageSource damageSource);
}
