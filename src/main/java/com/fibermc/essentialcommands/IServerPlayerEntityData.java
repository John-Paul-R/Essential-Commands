package com.fibermc.essentialcommands;

import net.minecraft.server.network.ServerPlayerEntity;

public interface IServerPlayerEntityData {
    ServerPlayerEntity getPlayer();

    void updatePlayerEntity(ServerPlayerEntity newPlayerEntity);
}
