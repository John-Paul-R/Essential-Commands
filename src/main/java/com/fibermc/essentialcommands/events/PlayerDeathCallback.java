package com.fibermc.essentialcommands.events;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface PlayerDeathCallback {
    Event<PlayerDeathCallback> EVENT = EventFactory.createArrayBacked(PlayerDeathCallback.class,
        (listeners) -> (player, damageSource) -> {
            for (PlayerDeathCallback event : listeners) {
                event.onDeath(player, damageSource);
            }
        });

    void onDeath(ServerPlayer playerEntity, DamageSource damageSource);
}
