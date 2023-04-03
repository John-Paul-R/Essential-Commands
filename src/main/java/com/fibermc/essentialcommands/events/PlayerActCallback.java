package com.fibermc.essentialcommands.events;

import net.minecraft.network.Packet;
import net.minecraft.network.listener.ServerPlayPacketListener;

public interface PlayerActCallback {
    void onPlayerAct(Packet<ServerPlayPacketListener> packet);
}
