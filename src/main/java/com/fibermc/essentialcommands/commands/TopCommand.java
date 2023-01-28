package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.teleportation.PlayerTeleporter;
import com.fibermc.essentialcommands.text.ECText;
import com.fibermc.essentialcommands.types.MinecraftLocation;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;

public class TopCommand implements Command<ServerCommandSource> {
    @SuppressWarnings("checkstyle:LocalVariableName")
    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();
        World world = source.getWorld();
        Vec3d playerPos = player.getPos();

        int new_y;
        double new_x = playerPos.x;
        double new_z = playerPos.z;

        final BlockPos targetXZ = new BlockPos(new_x, 0, new_z);

        Chunk chunk = world.getChunk(targetXZ);
        new_y = getTop(chunk, (int) new_x, (int) new_z);

        // Teleport the player
        PlayerTeleporter.requestTeleport(
            player,
            new MinecraftLocation(world.getRegistryKey(), new_x, new_y, new_z, player.getHeadYaw(), player.getPitch()),
            ECText.access(player).getText("cmd.top.location_name")
        );

        return 0;
    }

    public static int getTop(Chunk chunk, int x, int z) {
        final int maxY = calculateMaxY(chunk);
        final int bottomY = chunk.getBottomY();
        if (maxY <= bottomY) {
            return Integer.MIN_VALUE;
        }

        final BlockPos.Mutable mutablePos = new BlockPos.Mutable(x, maxY, z);
        boolean isAir1 = chunk.getBlockState(mutablePos).isAir(); // Block at head level
        boolean isAir2 = chunk.getBlockState(mutablePos.move(Direction.DOWN)).isAir(); // Block at feet level
        boolean isAir3; // Block below feet

        while (mutablePos.getY() > bottomY) {
            isAir3 = chunk.getBlockState(mutablePos.move(Direction.DOWN)).isAir();
            if (!isAir3 && isAir2 && isAir1) { // If there is a floor block and space for player body+head
                return mutablePos.getY() + 1;
            }

            isAir1 = isAir2;
            isAir2 = isAir3;
        }

        return Integer.MIN_VALUE;
    }

    private static int calculateMaxY(Chunk chunk) {
        final int maxY = chunk.getHeight();
        ChunkSection[] sections = chunk.getSectionArray();
        int maxSectionIndex = Math.min(sections.length - 1, maxY >> 4);

        for (int index = maxSectionIndex; index >= 0; --index) {
            if (!sections[index].isEmpty()) {
                return Math.min(index << 4 + 15, maxY);
            }
        }

        return Integer.MAX_VALUE;
    }

}
