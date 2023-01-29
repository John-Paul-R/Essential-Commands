package com.fibermc.essentialcommands.events;

import net.minecraft.server.network.ServerPlayerEntity;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface PlayerLeaveCallback {
    Event<PlayerLeaveCallback> EVENT = EventFactory.createArrayBacked(PlayerLeaveCallback.class,
        (listeners) -> (player) -> {
            for (PlayerLeaveCallback event : listeners) {
                event.onPlayerLeave(player);
            }
    });

    void onPlayerLeave(ServerPlayerEntity player);

}
