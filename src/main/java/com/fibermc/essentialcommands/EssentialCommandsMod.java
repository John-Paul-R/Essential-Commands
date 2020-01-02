package com.fibermc.essentialcommands;

//import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.ModInitializer;



public class EssentialCommandsMod implements /*DedicatedServer*/ModInitializer {

    @Override
	public void onInitialize/*Server*/() {
        
        //CommandRegistry.INSTANCE.register(false, new TestConsumer());
		EssentialCommandRegistry registry = new EssentialCommandRegistry();
		registry.register();

		System.out.println("Hello Fabric world!");
	}
}
