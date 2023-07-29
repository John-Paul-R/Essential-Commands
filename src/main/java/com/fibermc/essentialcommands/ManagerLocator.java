package com.fibermc.essentialcommands;

import java.util.HashMap;
import java.util.function.Consumer;

import com.fibermc.essentialcommands.commands.suggestions.OfflinePlayerRepo;
import com.fibermc.essentialcommands.playerdata.PlayerDataManager;
import com.fibermc.essentialcommands.teleportation.TeleportManager;

import net.minecraft.server.MinecraftServer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public final class ManagerLocator {

    private MinecraftServer server;
    private PlayerDataManager playerDataManager;
    private TeleportManager tpManager;
    private WorldDataManager worldDataManager;
    private OfflinePlayerRepo offlinePlayerRepo;
    private final HashMap<String, Consumer<MinecraftServer>> serverStartActions = new HashMap<>();

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

    private boolean serverStarted = false;

    public void onServerStart(MinecraftServer server) {
        this.server = server;
        this.playerDataManager = PlayerDataManager.getInstance();
        this.tpManager = TeleportManager.getInstance();
        this.worldDataManager = WorldDataManager.createForServer(server);
        this.offlinePlayerRepo = new OfflinePlayerRepo(server);
        ServerLifecycleEvents.SERVER_STARTED.register(server1 -> {
            serverStartActions.values().forEach(a -> a.accept(server));
            serverStarted = true;
        });
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

    public void runAndQueue(String key, Consumer<MinecraftServer> action) {
        serverStartActions.putIfAbsent(key, action);

        if (this.serverStarted) {
            action.accept(this.server);
        }
    }
}
