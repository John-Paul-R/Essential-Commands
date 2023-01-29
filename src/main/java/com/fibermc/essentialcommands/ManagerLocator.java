package com.fibermc.essentialcommands;

import com.fibermc.essentialcommands.commands.suggestions.OfflinePlayerRepo;
import com.fibermc.essentialcommands.playerdata.PlayerDataManager;
import com.fibermc.essentialcommands.teleportation.TeleportManager;

import net.minecraft.server.MinecraftServer;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;

public final class ManagerLocator {

    private PlayerDataManager playerDataManager;
    private TeleportManager tpManager;
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

    public static boolean playerDataEnabled() {
        return CONFIG.ENABLE_HOME
            || CONFIG.ENABLE_TPA
            || CONFIG.ENABLE_BACK
            || CONFIG.ENABLE_WARP
            || CONFIG.ENABLE_SPAWN
            || CONFIG.ENABLE_NICK
            || CONFIG.ENABLE_AFK
            ;
    }

    public static boolean teleportRequestEnabled() {
        return (CONFIG.ENABLE_TPA);
    }

    public void init() {
        if (playerDataEnabled()) {
            PlayerDataManager.init();
        }
        if (teleportRequestEnabled()) {
            TeleportManager.init();
        }
    }

    public void onServerStart(MinecraftServer server) {
        this.playerDataManager = PlayerDataManager.getInstance();
        this.tpManager = TeleportManager.getInstance();
        this.worldDataManager = WorldDataManager.createForServer(server);
        this.offlinePlayerRepo = new OfflinePlayerRepo(server);
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public TeleportManager getTpManager() {
        return tpManager;
    }

    public WorldDataManager getWorldDataManager() {
        return worldDataManager;
    }

    public OfflinePlayerRepo getOfflinePlayerRepo() {
        return offlinePlayerRepo;
    }
}
