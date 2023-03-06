package com.fibermc.essentialcommands;

import com.fibermc.essentialcommands.commands.suggestions.OfflinePlayerRepo;
import com.fibermc.essentialcommands.playerdata.PlayerDataManager;
import com.fibermc.essentialcommands.teleportation.TeleportManager;

import net.minecraft.server.MinecraftServer;

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

    public void init() {
        PlayerDataManager.init();
        TeleportManager.init();
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
