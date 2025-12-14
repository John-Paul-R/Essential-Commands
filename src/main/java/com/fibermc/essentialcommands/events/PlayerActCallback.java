package com.fibermc.essentialcommands.events;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerGamePacketListener;

public interface PlayerActCallback {
    void onPlayerAct(Packet<ServerGamePacketListener> packet);
}
