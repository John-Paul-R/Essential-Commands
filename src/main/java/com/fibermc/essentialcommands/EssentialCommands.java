package com.fibermc.essentialcommands;

//import net.fabricmc.api.DedicatedServerModInitializer;
import com.fibermc.essentialcommands.config.EssentialCommandsConfig;
import dev.jpcode.eccore.config.Config;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.api.ModInitializer;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;


public final class EssentialCommands implements ModInitializer {
	public static Logger LOGGER = LogManager.getLogger("EssentialCommands");
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
	public void onInitialize/*Server*/() {
		log(Level.INFO, "Mod Load Initiated.");

		//Load Preferences
		CONFIG.loadOrCreateProperties();

		//init mod stuff
		ManagerLocator managers = new ManagerLocator();
		managers.init();
		ServerLifecycleEvents.SERVER_STARTING.register((MinecraftServer server) -> {
			managers.onServerStart(server);
//			I just do "./config" If you want toe( be more proper, you can do
////				server.getRunDirectory().toPath().resolv"config")
		});

		ECPerms.init();
		
		//Register Mod
		EssentialCommandRegistry.register();

		if (CONFIG.CHECK_FOR_UPDATES.getValue()) {
			Updater.checkForUpdates();
		}

		log(Level.INFO, "Mod Load Complete.");
	}
}
