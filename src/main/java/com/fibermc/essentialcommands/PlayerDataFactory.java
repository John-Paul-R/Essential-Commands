package com.fibermc.essentialcommands;

import net.minecraft.server.network.ServerPlayerEntity;

public class PlayerDataFactory {

    public static PlayerData create(ServerPlayerEntity player) {
        return new PlayerData(player);
    }
}
