package com.fibermc.essentialcommands.events;

import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.packet.Packet;

public interface PlayerActCallback {
    void onPlayerAct(Packet<ServerPlayPacketListener> packet);
}
