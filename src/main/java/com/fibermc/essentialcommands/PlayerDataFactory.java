package com.fibermc.essentialcommands;

import net.minecraft.server.network.ServerPlayerEntity;

public class PlayerDataFactory {

    private static ManagerLocator _managers;
    public static void init(ManagerLocator managers) { _managers = managers; }
    public static PlayerData create(ServerPlayerEntity player) {
        return new PlayerData(player, _managers);
    }
}
