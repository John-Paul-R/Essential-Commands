package com.fibermc.essentialcommands;

//import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.api.ModInitializer;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public final class EssentialCommands implements ModInitializer {
	public static Logger LOGGER = LogManager.getLogger("EssentialCommands");

	public static void log(Level level, String message) {
		final String logPrefix = "[EssentialCommands]: ";
		LOGGER.log(level, logPrefix.concat(message));
	}


    @Override
	public void onInitialize/*Server*/() {
		log(Level.INFO, "Mod Load Initiated.");

		//Load Preferences
		Config.loadOrCreateProperties();

		//init mod stuff
		ManagerLocator managers = new ManagerLocator();
		managers.init();
		ServerLifecycleEvents.SERVER_STARTED.register((MinecraftServer server) -> {
			managers.onServerStart(server);
//			server.
		});
		ECPerms.init();
		
		//Register Mod
		EssentialCommandRegistry.register();

		if (Config.CHECK_FOR_UPDATES) {
			Updater.checkForUpdates();
		}

		log(Level.INFO, "Mod Load Complete.");
	}
}
