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

    static boolean playerDataEnabled() {
        return (
            Config.ENABLE_HOME  ||
            Config.ENABLE_TPA   ||
            Config.ENABLE_BACK  ||
            Config.ENABLE_WARP  ||
            Config.ENABLE_SPAWN
        );

    }
    static boolean teleportRequestEnabled() {
        return (Config.ENABLE_TPA);
    }
    static boolean warpEnabled() {
        return (Config.ENABLE_TPA || Config.ENABLE_SPAWN);
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
