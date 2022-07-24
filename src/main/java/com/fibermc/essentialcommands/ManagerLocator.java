package com.fibermc.essentialcommands;

import com.fibermc.essentialcommands.commands.suggestions.OfflinePlayerRepo;

import net.minecraft.server.MinecraftServer;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;

public final class ManagerLocator {

    private PlayerDataManager playerDataManager;
    private TeleportRequestManager tpManager;
    private WorldDataManager worldDataManager;
    private OfflinePlayerRepo offlinePlayerRepo;

    public static ManagerLocator instance;

    private ManagerLocator() {}

    public static ManagerLocator getInstance() {
        if (instance != null) {
            return instance;
        }
        return instance = new ManagerLocator();
    }

    static boolean playerDataEnabled() {
        return CONFIG.ENABLE_HOME
            || CONFIG.ENABLE_TPA
            || CONFIG.ENABLE_BACK
            || CONFIG.ENABLE_WARP
            || CONFIG.ENABLE_SPAWN
            || CONFIG.ENABLE_NICK
            ;
    }

    static boolean teleportRequestEnabled() {
        return (CONFIG.ENABLE_TPA);
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
        this.playerDataManager = PlayerDataManager.getInstance();
        this.tpManager = TeleportRequestManager.getInstance();
        this.worldDataManager = WorldDataManager.createForServer(server);
        this.offlinePlayerRepo = new OfflinePlayerRepo(server);
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

    public OfflinePlayerRepo getOfflinePlayerRepo() {
        return offlinePlayerRepo;
    }
}
