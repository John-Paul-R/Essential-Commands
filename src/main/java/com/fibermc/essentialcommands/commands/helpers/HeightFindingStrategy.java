package com.fibermc.essentialcommands.commands.helpers;

import java.util.OptionalInt;

import com.fibermc.essentialcommands.commands.utility.TopCommand;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

public enum HeightFindingStrategy implements HeightFinder {
    SKY_TO_SURFACE__FIRST_SOLID(TopCommand::getTop),
    BOTTOM_TO_SKY__FIRST_SAFE_AIR(HeightFindingStrategy::findYBottomUp),
    ;

    private final HeightFinder heightFinder;

    HeightFindingStrategy(HeightFinder heightFinder) {

        this.heightFinder = heightFinder;
    }

    public static HeightFindingStrategy forWorld(ResourceKey<Level> worldRegistryKey) {
        if (worldRegistryKey == Level.OVERWORLD || worldRegistryKey == Level.END) {
            return HeightFindingStrategy.SKY_TO_SURFACE__FIRST_SOLID;
        }
        if (worldRegistryKey == Level.NETHER) {
            return HeightFindingStrategy.BOTTOM_TO_SKY__FIRST_SAFE_AIR;
        }

        // fallback
        return HeightFindingStrategy.SKY_TO_SURFACE__FIRST_SOLID;
    }

    @Override
    public OptionalInt getY(ChunkAccess chunk, int x, int z) {
        return heightFinder.getY(chunk, x, z);
    }

    private static OptionalInt findYBottomUp(ChunkAccess chunk, int x, int z) {
        final int topY = getChunkHighestNonEmptySectionYOffsetOrTopY(chunk);
        final int bottomY = chunk.getMinY();
        if (topY <= bottomY) {
            return OptionalInt.empty();
        }

        final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos(x, bottomY, z);
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

    public static int getChunkHighestNonEmptySectionYOffsetOrTopY(ChunkAccess chunk) {
        int i = chunk.getHighestFilledSectionIndex();
        return i == chunk.getMaxY() ? chunk.getMinY() : SectionPos.sectionToBlockCoord(chunk.getSectionYFromSectionIndex(i));
    }
}
