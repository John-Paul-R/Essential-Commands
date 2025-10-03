package com.fibermc.essentialcommands.events;

import net.minecraft.server.network.ServerPlayerEntity;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface NicknameChangeCallback {
    Event<NicknameChangeCallback> EVENT = EventFactory.createArrayBacked(
        NicknameChangeCallback.class,
        (listeners) -> (player) -> {
            for (NicknameChangeCallback event : listeners) {
                event.onNicknameChange(player);
            }
        });

    void onNicknameChange(ServerPlayerEntity player);
}
