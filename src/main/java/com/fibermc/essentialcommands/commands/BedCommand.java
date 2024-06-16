package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.playerdata.PlayerData;
import com.fibermc.essentialcommands.teleportation.PlayerTeleporter;
import com.fibermc.essentialcommands.text.ECText;
import com.fibermc.essentialcommands.text.TextFormatType;
import com.fibermc.essentialcommands.types.MinecraftLocation;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.block.BedBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.entity.EntityType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.Objects;
import java.util.Optional;

public class BedCommand implements Command<ServerCommandSource> {
    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var player = context.getSource().getPlayerOrThrow();
        var spawnPos = player.getSpawnPointPosition();
        var spawnDim = player.getSpawnPointDimension();
        var spawnAngle = player.getSpawnAngle();

        if (spawnPos == null) {
            PlayerData.access(player).sendError("cmd.bed.error.none_set");
            return 0;
        }

        var world = Objects.requireNonNull(player.getServer()).getWorld(spawnDim);

        if (world == null) {
            throw new IllegalStateException(String.format(
                "could not resolve the ServerWorld corresponding to the player's spawn dimension. dimension: '%s'",
                spawnDim.getValue())
            );
        }

        //Safe Position Calculation, based on the game respawn position calculation logic,
        //which was basically rewritten because the game code caused the state of the RespawnAnchorBlock to be refreshed.
        Vec3d safeSpawnPos;
        BlockState blockState = world.getBlockState(spawnPos);
        Block block = blockState.getBlock();
        if (block instanceof RespawnAnchorBlock
            && (Integer)blockState.get(RespawnAnchorBlock.CHARGES) > 0 && RespawnAnchorBlock.isNether(world)) {
            Optional<Vec3d> optional = RespawnAnchorBlock.findRespawnPosition(EntityType.PLAYER, world, spawnPos);
            safeSpawnPos = optional.orElseGet(() -> new Vec3d((double)spawnPos.getX() + 0.5, (double)spawnPos.getY() + 1, (double)spawnPos.getZ() + 0.5));
        } else if (block instanceof BedBlock && BedBlock.isBedWorking(world)) {
            Optional<Vec3d> optional =  BedBlock.findWakeUpPosition(EntityType.PLAYER, world, spawnPos, (Direction)blockState.get(BedBlock.FACING), spawnAngle);
            safeSpawnPos = optional.orElseGet(() -> new Vec3d((double)spawnPos.getX() + 0.5, (double)spawnPos.getY() + 0.5625, (double)spawnPos.getZ() + 0.5));
        } else {
            boolean bl = block.canMobSpawnInside(blockState);
            BlockState blockState2 = world.getBlockState(spawnPos.up());
            boolean bl2 = blockState2.getBlock().canMobSpawnInside(blockState2);
            if(bl && bl2) {
                safeSpawnPos = new Vec3d((double)spawnPos.getX() + 0.5, (double)spawnPos.getY() + 0.1, (double)spawnPos.getZ() + 0.5);
            }else{
                safeSpawnPos = Vec3d.ofBottomCenter(spawnPos);
            }
        }

        PlayerTeleporter.requestTeleport(
            player,
            new MinecraftLocation(spawnDim, safeSpawnPos.getX(), safeSpawnPos.getY(), safeSpawnPos.getZ()),
            ECText.access(player).getText("cmd.bed.bed_destination_name", TextFormatType.Accent));

        return 0;
    }
}
