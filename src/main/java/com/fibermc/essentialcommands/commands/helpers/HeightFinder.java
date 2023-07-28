package com.fibermc.essentialcommands.commands.helpers;

import java.util.OptionalInt;

import net.minecraft.world.chunk.Chunk;

@FunctionalInterface
public interface HeightFinder {
    OptionalInt getY(Chunk chunk, int x, int z);
}

