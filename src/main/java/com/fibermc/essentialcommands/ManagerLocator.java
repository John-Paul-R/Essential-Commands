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

    private static boolean playerDataEnabled() {
        return (Config.ENABLE_HOME || Config.ENABLE_TPA || Config.ENABLE_BACK);
    }
    private static boolean teleportRequestEnabled() {
        return (Config.ENABLE_TPA);
    }
    private static boolean warpEnabled() {
        return (Config.ENABLE_TPA);
    }

    public void init() {
        if (playerDataEnabled()) {
            PlayerDataManager.init();
        }
        if (teleportRequestEnabled()) {
            TeleportRequestManager.init();
        }
    }

    public void onServerStart(MinecraftServer server) {
        if (playerDataEnabled()) {
            this.playerDataManager = new PlayerDataManager();
        }
        if (teleportRequestEnabled()) {
            this.tpManager = new TeleportRequestManager(this.playerDataManager);
        }
        if (warpEnabled()) {
            this.worldDataManager = new WorldDataManager();
            this.worldDataManager.onServerStart(server);
        }
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
