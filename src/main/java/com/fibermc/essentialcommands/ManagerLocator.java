package com.fibermc.essentialcommands;

import net.minecraft.server.MinecraftServer;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;

public class ManagerLocator {

    private PlayerDataManager playerDataManager;
    private TeleportRequestManager tpManager;
    private WorldDataManager worldDataManager;

    public static ManagerLocator INSTANCE;

    private ManagerLocator() {
        INSTANCE = this;
    }

    public static ManagerLocator getInstance() {
        if (INSTANCE != null)
            return INSTANCE;
        return new ManagerLocator();
    };

    static boolean playerDataEnabled() {
        return (
            CONFIG.ENABLE_HOME.getValue()  ||
            CONFIG.ENABLE_TPA.getValue()   ||
            CONFIG.ENABLE_BACK.getValue()  ||
            CONFIG.ENABLE_WARP.getValue()  ||
            CONFIG.ENABLE_SPAWN.getValue() ||
            CONFIG.ENABLE_NICK.getValue()
        );

    }
    static boolean teleportRequestEnabled() {
        return (CONFIG.ENABLE_TPA.getValue());
    }
    static boolean warpEnabled() {
        return (CONFIG.ENABLE_TPA.getValue() || CONFIG.ENABLE_SPAWN.getValue());
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
