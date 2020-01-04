package com.fibermc.essentialcommands.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.damage.DamageSource;

import java.util.UUID;

public interface PlayerDeathCallback {
    public Event<PlayerDeathCallback> EVENT = EventFactory.createArrayBacked(PlayerDeathCallback.class,
            (listeners) -> (playerID, damageSource) -> {
                for (PlayerDeathCallback event : listeners) {
                    event.onDeath(playerID, damageSource);
                }
            });

    void onDeath(UUID playerID, DamageSource damageSource);
}
