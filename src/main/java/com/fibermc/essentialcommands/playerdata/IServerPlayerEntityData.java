package com.fibermc.essentialcommands.playerdata;

import net.minecraft.server.level.ServerPlayer;

public interface IServerPlayerEntityData {
    ServerPlayer getPlayer();

    void updatePlayerEntity(ServerPlayer newPlayerEntity);
}
