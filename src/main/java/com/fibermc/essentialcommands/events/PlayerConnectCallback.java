package com.fibermc.essentialcommands.events;

import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface PlayerConnectCallback {
    Event<PlayerConnectCallback> EVENT = EventFactory.createArrayBacked(
        PlayerConnectCallback.class,
        (listeners) -> (connection, player) -> {
            for (PlayerConnectCallback event : listeners) {
                event.onPlayerConnect(connection, player);
            }
        });

    void onPlayerConnect(Connection connection, ServerPlayer player);
}
