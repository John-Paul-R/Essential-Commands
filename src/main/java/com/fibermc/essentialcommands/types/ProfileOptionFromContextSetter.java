package com.fibermc.essentialcommands.types;

import com.fibermc.essentialcommands.PlayerProfile;

import com.mojang.brigadier.context.CommandContext;

import net.minecraft.server.command.ServerCommandSource;

@FunctionalInterface
public interface ProfileOptionFromContextSetter<T> {
    void setValue(CommandContext<ServerCommandSource> context, String name, PlayerProfile profile);
}
