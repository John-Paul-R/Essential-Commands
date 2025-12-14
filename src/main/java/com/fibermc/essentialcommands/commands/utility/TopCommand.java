package com.fibermc.essentialcommands.commands.utility;

import java.util.OptionalInt;

import com.fibermc.essentialcommands.teleportation.PlayerTeleporter;
import com.fibermc.essentialcommands.text.ECText;
import com.fibermc.essentialcommands.types.MinecraftLocation;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;

public class TopCommand implements Command<CommandSourceStack> {
    @SuppressWarnings("checkstyle:LocalVariableName")
    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        Level world = source.getLevel();
        BlockPos playerPos = player.blockPosition();

        OptionalInt new_y;
        int new_x = playerPos.getX();
        int new_z = playerPos.getZ();

        final BlockPos targetXZ = new BlockPos(new_x, 0, new_z);

        ChunkAccess chunk = world.getChunk(targetXZ);
        new_y = getTop(chunk, new_x, new_z);

        if (new_y.isEmpty()) {
            // TODO: err msg
            return -1;
        }
        // Teleport the player
        PlayerTeleporter.requestTeleport(
            player,
            new MinecraftLocation(world.dimension(), new_x, new_y.getAsInt(), new_z, player.getYHeadRot(), player.getXRot()),
            ECText.access(player).getText("cmd.top.location_name")
        );

        return 0;
    }

    public static OptionalInt getTop(ChunkAccess chunk, int x, int z) {
        final int maxY = calculateMaxY(chunk);
        final int bottomY = chunk.getMinY();
        if (maxY <= bottomY) {
            return OptionalInt.empty();
        }

        final BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos(x, maxY, z);
        boolean isAir1 = chunk.getBlockState(mutablePos).isAir(); // Block at head level
        boolean isAir2 = chunk.getBlockState(mutablePos.move(Direction.DOWN)).isAir(); // Block at feet level
        boolean isAir3; // Block below feet

        while (mutablePos.getY() > bottomY) {
            isAir3 = chunk.getBlockState(mutablePos.move(Direction.DOWN)).isAir();
            if (!isAir3 && isAir2 && isAir1) { // If there is a floor block and space for player body+head
                return OptionalInt.of(mutablePos.getY() + 1);
            }

            isAir1 = isAir2;
            isAir2 = isAir3;
        }

        return OptionalInt.empty();
    }

    private static int calculateMaxY(ChunkAccess chunk) {
        final int maxY = chunk.getMaxY();
        LevelChunkSection[] sections = chunk.getSections();
        int maxSectionIndex = Math.min(sections.length - 1, maxY >> 4);

        for (int index = maxSectionIndex; index >= 0; --index) {
            if (!sections[index].hasOnlyAir()) {
                return Math.min(index << 4 + 15, maxY);
            }
        }

        return Integer.MAX_VALUE;
    }

}
