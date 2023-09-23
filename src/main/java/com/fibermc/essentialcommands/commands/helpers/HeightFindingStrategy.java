package com.fibermc.essentialcommands.commands.helpers;

import java.util.OptionalInt;

import com.fibermc.essentialcommands.commands.utility.TopCommand;

import net.minecraft.block.BlockState;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public enum HeightFindingStrategy implements HeightFinder {
    SKY_TO_SURFACE__FIRST_SOLID(TopCommand::getTop),
    BOTTOM_TO_SKY__FIRST_SAFE_AIR(HeightFindingStrategy::findYBottomUp),
    ;

    private final HeightFinder heightFinder;

    HeightFindingStrategy(HeightFinder heightFinder) {

        this.heightFinder = heightFinder;
    }

    public static HeightFindingStrategy forWorld(RegistryKey<World> worldRegistryKey) {
        if (worldRegistryKey == World.OVERWORLD || worldRegistryKey == World.END) {
            return HeightFindingStrategy.SKY_TO_SURFACE__FIRST_SOLID;
        }
        if (worldRegistryKey == World.NETHER) {
            return HeightFindingStrategy.BOTTOM_TO_SKY__FIRST_SAFE_AIR;
        }

        // fallback
        return HeightFindingStrategy.SKY_TO_SURFACE__FIRST_SOLID;
    }

    @Override
    public OptionalInt getY(Chunk chunk, int x, int z) {
        return heightFinder.getY(chunk, x, z);
    }

    private static OptionalInt findYBottomUp(Chunk chunk, int x, int z) {
        final int topY = getChunkHighestNonEmptySectionYOffsetOrTopY(chunk);
        final int bottomY = chunk.getBottomY();
        if (topY <= bottomY) {
            return OptionalInt.empty();
        }

        final BlockPos.Mutable mutablePos = new BlockPos.Mutable(x, bottomY, z);
        BlockState bsFeet1 = chunk.getBlockState(mutablePos); // Block below feet
        BlockState bsBody2 = chunk.getBlockState(mutablePos.move(Direction.UP)); // Block at feet level
        BlockState bsHead3; // Block at head level

        while (mutablePos.getY() < topY) {
            bsHead3 = chunk.getBlockState(mutablePos.move(Direction.UP));
            if (bsFeet1.isSolid() && bsBody2.isAir() && bsHead3.isAir()) { // If there is a floor block and space for player body+head
                return OptionalInt.of(mutablePos.getY() - 1);
            }

            bsFeet1 = bsBody2;
            bsBody2 = bsHead3;
        }

        return OptionalInt.empty();
    }

    public static int getChunkHighestNonEmptySectionYOffsetOrTopY(Chunk chunk) {
        int i = chunk.getHighestNonEmptySection();
        return i == chunk.getTopY() ? chunk.getBottomY() : ChunkSectionPos.getBlockCoord(chunk.sectionIndexToCoord(i));
    }
}
