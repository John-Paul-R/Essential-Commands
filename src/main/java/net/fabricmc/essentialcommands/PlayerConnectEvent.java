package net.fabricmc.essentialcommands;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

/**
 * PlayerConnectEvent
 */
public class PlayerConnectEvent extends Event<> {

    @Override
    public void register(Object listener) {
        // TODO Auto-generated method stub
        EventFactory.createArrayBacked(type, invokerFactory)
    
    }


}