package com.fibermc.essentialcommands.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.network.ServerPlayerEntity;

public interface PlayerConnectCallback {
    Event<PlayerConnectCallback> EVENT = EventFactory.createArrayBacked(PlayerConnectCallback.class,
        (listeners) -> (connection, player) -> {
            for (PlayerConnectCallback event : listeners) {
                event.onPlayerConnect(connection, player);
            }
    });
 
    void onPlayerConnect(ClientConnection connection, ServerPlayerEntity player);
}