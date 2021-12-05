package com.fibermc.essentialcommands.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;

public interface PlayerDeathCallback {
    Event<PlayerDeathCallback> EVENT = EventFactory.createArrayBacked(PlayerDeathCallback.class,
        (listeners) -> (player, damageSource) -> {
            for (PlayerDeathCallback event : listeners) {
                event.onDeath(player, damageSource);
            }
        });

    void onDeath(ServerPlayerEntity playerEntity, DamageSource damageSource);
}
