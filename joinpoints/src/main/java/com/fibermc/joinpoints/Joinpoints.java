package com.fibermc.joinpoints;

import com.fibermc.joinpoints.config.JoinpointsConfig;
import com.fibermc.joinpoints.database.JoinpointDatabase;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Joinpoints implements ModInitializer {
    public static final String MOD_ID = "joinpoints";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    private static JoinpointsConfig config;
    private static JoinpointDatabase database;
    private static MinecraftServer server;

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Joinpoints mod");

        // Register commands
        JoinpointsCommandRegistry.register();

        ServerLifecycleEvents.SERVER_STARTING.register(this::onServerStarting);
        ServerLifecycleEvents.SERVER_STOPPED.register(this::onServerStopped);
    }

    private void onServerStarting(MinecraftServer server) {
        Joinpoints.server = server;

        // Initialize config
        config = new JoinpointsConfig(
            server.getSavePath(net.minecraft.util.WorldSavePath.ROOT).resolve("config").resolve("joinpoints").resolve("joinpoints.properties"),
            "Joinpoints Configuration",
            "https://github.com/John-Paul-R/Essential-Commands" // TODO: Update with joinpoints docs link
        );
        config.loadOrCreateProperties();

        // Initialize database
        database = new JoinpointDatabase(server.getSavePath(net.minecraft.util.WorldSavePath.ROOT).toFile());

        LOGGER.info("Joinpoints mod initialized successfully");
    }

    private void onServerStopped(MinecraftServer server) {
        Joinpoints.server = null;
    }

    public static JoinpointsConfig getConfig() {
        return config;
    }

    public static JoinpointDatabase getDatabase() {
        return database;
    }

    public static MinecraftServer getServer() {
        return server;
    }
}
