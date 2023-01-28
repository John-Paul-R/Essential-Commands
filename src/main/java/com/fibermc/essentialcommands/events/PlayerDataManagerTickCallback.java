package com.fibermc.essentialcommands.events;

import com.fibermc.essentialcommands.playerdata.PlayerDataManager;

import net.minecraft.server.MinecraftServer;

public interface PlayerDataManagerTickCallback {

    void onTick(PlayerDataManager playerDataManager, MinecraftServer server);
}
