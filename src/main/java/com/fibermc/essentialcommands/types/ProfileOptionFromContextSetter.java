package com.fibermc.essentialcommands.types;

import com.fibermc.essentialcommands.playerdata.PlayerProfile;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;

@FunctionalInterface
public interface ProfileOptionFromContextSetter<T> {
    void setValue(CommandContext<CommandSourceStack> context, String name, PlayerProfile profile)
        throws CommandSyntaxException;
}
