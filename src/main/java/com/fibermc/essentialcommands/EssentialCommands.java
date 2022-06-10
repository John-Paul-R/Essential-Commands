package com.fibermc.essentialcommands;

import com.fibermc.essentialcommands.config.EssentialCommandsConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;

public final class EssentialCommands implements ModInitializer {
    public static final ModMetadata MOD_METADATA = FabricLoader.getInstance().getModContainer("essential_commands").get().getMetadata();
    public static final String MOD_ID = MOD_METADATA.getId();
    public static final Logger LOGGER = LogManager.getLogger("EssentialCommands");
    public static final EssentialCommandsConfig CONFIG = new EssentialCommandsConfig(
        Path.of("./config/EssentialCommands.properties"),
        "Essential Commands Config",
        "https://github.com/John-Paul-R/Essential-Commands/wiki/Config-Documentation"
    );
    public static void log(Level level, String message) {
        final String logPrefix = "[EssentialCommands]: ";
        LOGGER.log(level, logPrefix.concat(message));
    }

    @Override
    public void onInitialize() {
        log(Level.INFO, "Mod Load Initiated.");

        CONFIG.loadOrCreateProperties();

        ECPlaceholderRegistry.register();

        ManagerLocator managers = ManagerLocator.getInstance();
        managers.init();
        ServerLifecycleEvents.SERVER_STARTING.register((server) -> {
            managers.onServerStart(server);
            ECPerms.init(); // ECPerms must start after WorldDataManager at present (for warps).
        });

        CommandRegistrationCallback.EVENT.register(EssentialCommandRegistry::register);

        if (CONFIG.CHECK_FOR_UPDATES.getValue()) {
            Updater.checkForUpdates();
        }

        log(Level.INFO, "Mod Load Complete.");
    }
}
