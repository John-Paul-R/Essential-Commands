package net.fabricmc.essentialcommands;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;

public interface PlayerLeaveCallback {
    public Event<PlayerLeaveCallback> EVENT = EventFactory.createArrayBacked(PlayerLeaveCallback.class,
        (listeners) -> (player) -> {
            for (PlayerLeaveCallback event : listeners) {

                event.onPlayerLeave(player);
            }
    });

    void onPlayerLeave(ServerPlayerEntity player);
}
