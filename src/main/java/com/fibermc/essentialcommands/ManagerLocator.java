package com.fibermc.essentialcommands;

import com.fibermc.essentialcommands.commands.suggestions.OfflinePlayerRepo;
import net.minecraft.server.MinecraftServer;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;

public class ManagerLocator {

    private PlayerDataManager playerDataManager;
    private TeleportRequestManager tpManager;
    private WorldDataManager worldDataManager;
    private OfflinePlayerRepo offlinePlayerRepo;

    public static ManagerLocator INSTANCE;

    private ManagerLocator() {
        INSTANCE = this;
    }

    public static ManagerLocator getInstance() {
        if (INSTANCE != null)
            return INSTANCE;
        return new ManagerLocator();
    }

    static boolean playerDataEnabled() {
        return (CONFIG.ENABLE_HOME.getValue()
            || CONFIG.ENABLE_TPA.getValue()
            || CONFIG.ENABLE_BACK.getValue()
            || CONFIG.ENABLE_WARP.getValue()
            || CONFIG.ENABLE_SPAWN.getValue()
            || CONFIG.ENABLE_NICK.getValue()
        );
    }

    static boolean teleportRequestEnabled() {
        return (CONFIG.ENABLE_TPA.getValue());
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
