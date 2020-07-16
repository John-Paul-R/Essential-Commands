package com.fibermc.essentialcommands;

//import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public final class EssentialCommands implements /*DedicatedServer*/ModInitializer {
	public static Logger LOGGER = LogManager.getLogger("EssentialCommands");
//	private static PlayerDataManager _dataManager;
//	private static TeleportRequestManager _tpManager;

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
		PlayerDataManager dataManager = new PlayerDataManager();
		TeleportRequestManager tpManager = new TeleportRequestManager(dataManager);
		ManagerLocator managers = new ManagerLocator(dataManager, tpManager);
		PlayerDataFactory.init(managers);

		//Register Mod
		EssentialCommandRegistry.register(managers);

		log(Level.INFO, "Mod Load Complete.");
	}
}
