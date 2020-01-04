package com.fibermc.essentialcommands;

//import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public final class EssentialCommands implements /*DedicatedServer*/ModInitializer {
	public static Logger LOGGER = LogManager.getLogger("EssentialCommands");

    @Override
	public void onInitialize/*Server*/() {
		LOGGER.info("Mod Load Initiated.");
		//Load Preferences
		Config.loadOrCreateProperties();
		//Register Mod
		EssentialCommandRegistry registry = new EssentialCommandRegistry();
		registry.register();

		LOGGER.info("Mod Load Complete.");
	}
}
