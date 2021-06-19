package com.fibermc.essentialcommands;

import net.minecraft.server.MinecraftServer;

public class ManagerLocator {

    private PlayerDataManager playerDataManager;
    private TeleportRequestManager tpManager;
    private WorldDataManager worldDataManager;

    public static ManagerLocator INSTANCE;

    public ManagerLocator() {
        INSTANCE = this;
    }

    public void init() {
        PlayerDataManager.init();
        TeleportRequestManager.init();
    }

    public void onServerStart(MinecraftServer server) {
        this.playerDataManager = new PlayerDataManager();
        this.tpManager = new TeleportRequestManager(this.playerDataManager);
        this.worldDataManager = new WorldDataManager();
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
