package com.fibermc.joinpoints;

import com.fibermc.joinpoints.config.JoinpointsConfig;
import com.fibermc.joinpoints.database.JoinpointDatabase;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;

public class Joinpoints implements ModInitializer {
    public static final String MOD_ID = "joinpoints";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    private static final JoinpointsConfig config = new JoinpointsConfig(
        Path.of("./config/joinpoints.properties"),
        "Joinpoints Configuration",
        "https://github.com/John-Paul-R/Essential-Commands" // TODO: Update with joinpoints docs link
    );
    private static JoinpointDatabase database;
    private static MinecraftServer server;

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Joinpoints mod");

        // Initialize config early
        config.loadOrCreateProperties();

        // Register commands
        JoinpointsCommandRegistry.register();

        ServerLifecycleEvents.SERVER_STARTING.register(this::onServerStarting);
        ServerLifecycleEvents.SERVER_STOPPED.register(this::onServerStopped);
    }

    private void onServerStarting(MinecraftServer server) {
        Joinpoints.server = server;

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
