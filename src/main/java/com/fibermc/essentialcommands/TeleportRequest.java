package com.fibermc.essentialcommands;

import com.fibermc.essentialcommands.types.MinecraftLocation;
import net.minecraft.server.MinecraftServer;

public interface TeleportRequest {

//    TeleportRequest(ServerPlayerEntity sourcePlayer, ServerPlayerEntity targetPlayer);
//    TeleportRequest(ServerPlayerEntity sourcePlayer, MinecraftLocation targetLocation);

    MinecraftLocation getTargetLocation();

    void startRequest();

    void tick(MinecraftServer server);


}
