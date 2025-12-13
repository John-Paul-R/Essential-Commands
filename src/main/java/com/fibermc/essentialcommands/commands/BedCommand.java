package com.fibermc.essentialcommands.commands;

import java.util.Optional;

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
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.attribute.EnvironmentAttributes;

public class BedCommand implements Command<ServerCommandSource> {
    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var source = context.getSource();
        var player = source.getPlayerOrThrow();

        var safeSpawnPos = getSafeSpawnPos(player);
        if (safeSpawnPos.isEmpty()) {
            PlayerData.access(player).sendError("cmd.bed.error.none_set");
            return 0;
        }

        PlayerTeleporter.requestTeleport(
            player,
            safeSpawnPos.get(),
            ECText.access(player).getText("cmd.bed.bed_destination_name", TextFormatType.Accent));

        return 0;
    }

    /**
     * This function finds a "safe" spawn position for a player based on their spawnpoint. This
     * calculation differs based on how the spawnpoint was set (respawn anchor, bed, etc.)
     * Returns Optional.empty() only when the player has no spawn point set. (at time of writing,
     * if a "safe" spawnpoint cannot be found, we'll return a point just above the respawn target
     * block)
     */
    private static Optional<MinecraftLocation> getSafeSpawnPos(ServerPlayerEntity player) {
        var respawn = player.getRespawn();
        if (respawn == null) {
            return Optional.empty();
        }

        var respawnPosData = respawn.respawnData().globalPos();
        ServerWorld world = player.getEntityWorld().getServer().getWorld(respawnPosData.dimension());
        var spawnPos = respawnPosData.pos();

        // Safe Position Calculation, based on the game respawn position calculation logic,
        // which was basically rewritten because the game code caused the state of the RespawnAnchorBlock to be refreshed.
        Vec3d safeSpawnPos;
        BlockState blockState = world.getBlockState(spawnPos);
        Block block = blockState.getBlock();
        if (block instanceof RespawnAnchorBlock
            && blockState.get(RespawnAnchorBlock.CHARGES) > 0 && RespawnAnchorBlock.isUsable(world, spawnPos)
        ) {
            Optional<Vec3d> optional = RespawnAnchorBlock.findRespawnPosition(EntityType.PLAYER, world, spawnPos);
            safeSpawnPos = optional.orElseGet(() -> new Vec3d((double) spawnPos.getX() + 0.5, (double) spawnPos.getY() + 1, (double) spawnPos.getZ() + 0.5));
        } else if (block instanceof BedBlock && world.getEnvironmentAttributes().getAttributeValue(EnvironmentAttributes.BED_RULE_GAMEPLAY, spawnPos).canSetSpawn(world)) {
            Optional<Vec3d> optional = BedBlock.findWakeUpPosition(EntityType.PLAYER, world, spawnPos, blockState.get(BedBlock.FACING), respawn.respawnData().pitch());
            safeSpawnPos = optional.orElseGet(() -> new Vec3d((double) spawnPos.getX() + 0.5, (double) spawnPos.getY() + 0.5625, (double) spawnPos.getZ() + 0.5));
        } else {
            boolean bl = block.canMobSpawnInside(blockState);
            BlockState blockState2 = world.getBlockState(spawnPos.up());
            boolean bl2 = blockState2.getBlock().canMobSpawnInside(blockState2);
            if (bl && bl2) {
                safeSpawnPos = new Vec3d((double) spawnPos.getX() + 0.5, (double) spawnPos.getY() + 0.1, (double) spawnPos.getZ() + 0.5);
            } else {
                safeSpawnPos = Vec3d.ofBottomCenter(spawnPos);
            }
        }

        return Optional.of(new MinecraftLocation(respawnPosData.dimension(), safeSpawnPos.getX(), safeSpawnPos.getY(), safeSpawnPos.getZ()));
    }
}
