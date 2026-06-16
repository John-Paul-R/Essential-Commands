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

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RespawnAnchorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class BedCommand implements Command<CommandSourceStack> {
    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var source = context.getSource();
        var player = source.getPlayerOrException();

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
    private static Optional<MinecraftLocation> getSafeSpawnPos(ServerPlayer player) {
        var respawn = player.getRespawnConfig();
        if (respawn == null) {
            return Optional.empty();
        }

        var respawnPosData = respawn.respawnData().globalPos();
        ServerLevel world = player.level().getServer().getLevel(respawnPosData.dimension());
        var spawnPos = respawnPosData.pos();

        // Safe Position Calculation, based on the game respawn position calculation logic,
        // which was basically rewritten because the game code caused the state of the RespawnAnchorBlock to be refreshed.
        Vec3 safeSpawnPos;
        BlockState blockState = world.getBlockState(spawnPos);
        Block block = blockState.getBlock();
        if (block instanceof RespawnAnchorBlock
            && blockState.getValue(RespawnAnchorBlock.CHARGE) > 0 && RespawnAnchorBlock.canSetSpawn(world, spawnPos)
        ) {
            Optional<Vec3> optional = RespawnAnchorBlock.findStandUpPosition(EntityTypes.PLAYER, world, spawnPos);
            safeSpawnPos = optional.orElseGet(() -> new Vec3((double) spawnPos.getX() + 0.5, (double) spawnPos.getY() + 1, (double) spawnPos.getZ() + 0.5));
        } else if (block instanceof BedBlock && world.environmentAttributes().getValue(EnvironmentAttributes.BED_RULE, spawnPos).canSetSpawn(world)) {
            Optional<Vec3> optional = BedBlock.findStandUpPosition(EntityTypes.PLAYER, world, spawnPos, blockState.getValue(BedBlock.FACING), respawn.respawnData().pitch());
            safeSpawnPos = optional.orElseGet(() -> new Vec3((double) spawnPos.getX() + 0.5, (double) spawnPos.getY() + 0.5625, (double) spawnPos.getZ() + 0.5));
        } else {
            boolean bl = block.isPossibleToRespawnInThis(blockState);
            BlockState blockState2 = world.getBlockState(spawnPos.above());
            boolean bl2 = blockState2.getBlock().isPossibleToRespawnInThis(blockState2);
            if (bl && bl2) {
                safeSpawnPos = new Vec3((double) spawnPos.getX() + 0.5, (double) spawnPos.getY() + 0.1, (double) spawnPos.getZ() + 0.5);
            } else {
                safeSpawnPos = Vec3.atBottomCenterOf(spawnPos);
            }
        }

        return Optional.of(new MinecraftLocation(respawnPosData.dimension(), safeSpawnPos.x(), safeSpawnPos.y(), safeSpawnPos.z()));
    }
}
