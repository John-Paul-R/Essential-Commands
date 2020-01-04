package com.fibermc.essentialcommands.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;

public interface PlayerRespawnCallback {
    public Event<PlayerRespawnCallback> EVENT = EventFactory.createArrayBacked(PlayerRespawnCallback.class,
            (listeners) -> (player) -> {
                for (PlayerRespawnCallback event : listeners) {
                    event.onPlayerRespawn(player);
                }
            });

    void onPlayerRespawn(ServerPlayerEntity player);

}
