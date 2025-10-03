package com.fibermc.joinpoints;

import java.nio.file.Path;

import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.events.NicknameChangeCallback;
import com.fibermc.essentialcommands.events.PlayerConnectCallback;
import com.fibermc.essentialcommands.text.ECText;
import com.fibermc.joinpoints.config.JoinpointsConfig;
import com.fibermc.joinpoints.database.JoinpointDatabase;
import com.google.gson.JsonElement;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mojang.serialization.JsonOps;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TextCodecs;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

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

        // Register joinpoints lang file with ECText
        ECText.registerAdditionalLangPath("/assets/joinpoints/lang/%s.json");

        // Register commands
        JoinpointsCommandRegistry.register();

        // Register player connect callback to update player cache
        PlayerConnectCallback.EVENT.register((connection, player) -> updatePlayerCache(player));

        // Register nickname change callback to update player cache
        NicknameChangeCallback.EVENT.register(Joinpoints::updatePlayerCache);

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

    private static void updatePlayerCache(ServerPlayerEntity player) {
        try {
            var playerData = ((ServerPlayerEntityAccess) player).ec$getPlayerData();

            String nicknameJson = playerData.getNickname()
                .map(text -> TextCodecs.CODEC.encodeStart(JsonOps.INSTANCE, text).getOrThrow())
                .map(JsonElement::toString)
                .orElse(null);
            database
                .updatePlayerCacheAsync(player.getUuid(), player.getName().getString(), nicknameJson)
                .exceptionally(err -> {
                    LOGGER.error(err);
                    return null;
                });
        } catch (Exception e) {
            // Log but don't crash on cache update failure - joinpoint database might not be initialized yet
            LOGGER.error(e);
        }
    }
}
