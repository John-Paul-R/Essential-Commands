package com.fibermc.essentialcommands.commands.helpers;

import java.util.OptionalInt;

import net.minecraft.world.level.chunk.ChunkAccess;

@FunctionalInterface
public interface HeightFinder {
    /**
     * Attempts to find a safe surface Y value for the specified X & Z values.
     *
     * @return A Y value corresponding to the player's feet pos
     */
    OptionalInt getY(ChunkAccess chunk, int x, int z);
}

