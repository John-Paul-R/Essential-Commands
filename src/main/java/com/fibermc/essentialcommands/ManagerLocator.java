package com.fibermc.essentialcommands;

import net.minecraft.server.MinecraftServer;

public class ManagerLocator {

    private final PlayerDataManager playerDataManager;
    private final TeleportRequestManager tpManager;
    private final WorldDataManager worldDataManager;

    public ManagerLocator() {
        this.playerDataManager = new PlayerDataManager();
        this.tpManager = new TeleportRequestManager(this.playerDataManager);
        this.worldDataManager = new WorldDataManager();
    }

    public void init(MinecraftServer server) {
        this.worldDataManager.init(server);
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public TeleportRequestManager getTpManager() {
        return tpManager;
    }

    public WorldDataManager getWorldDataManager() {
        return worldDataManager;
    }
}
