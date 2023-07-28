package com.fibermc.essentialcommands.commands;

import java.util.Optional;
import java.util.Random;

import com.fibermc.essentialcommands.EssentialCommands;
import com.fibermc.essentialcommands.ManagerLocator;
import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.playerdata.PlayerData;
import com.fibermc.essentialcommands.teleportation.PlayerTeleporter;
import com.fibermc.essentialcommands.text.ECText;
import com.fibermc.essentialcommands.text.TextFormatType;
import com.fibermc.essentialcommands.types.MinecraftLocation;
import com.google.common.base.Stopwatch;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.command.CommandException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import dev.jpcode.eccore.util.TextUtil;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;
import static com.fibermc.essentialcommands.commands.TopCommand.getTop;

/**
 * <p>
 * Heavily referenced from
 * https://github.com/javachaos/randomteleport/blob/master/src/main/java/net.ethermod/commands/RandomTeleportCommand.java
 * </p>
 * <p>
 * Additionally, tons of optimization tips & examples provided by @Wesley1808 on GitHub:
 * https://github.com/Wesley1808/ServerCore/issues/16
 * </p>
 */
@SuppressWarnings("checkstyle:all")
public class RandomTeleportCommand implements Command<ServerCommandSource> {

    public RandomTeleportCommand() {}

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        ServerWorld world = context.getSource().getWorld();
        var ecText = ECText.access(player);
        if (!world.getRegistryKey().equals(World.OVERWORLD)) {
            throw new CommandException(TextUtil.concat(
                ecText.getText("cmd.rtp.error.pre", TextFormatType.Error),
                ecText.getText("cmd.rtp.error.not_overworld", TextFormatType.Error)
            ));
        }

        //TODO Add OP/Permission bypass for RTP cooldown.
        if (CONFIG.RTP_COOLDOWN > 0) {
            ServerCommandSource source = context.getSource();
            int curServerTickTime = source.getServer().getTicks();
            PlayerData playerData = ((ServerPlayerEntityAccess) player).ec$getPlayerData();
            var rtpCooldownEndTime = playerData.getTimeUsedRtp() + CONFIG.RTP_COOLDOWN * 20;
            var rtpCooldownRemaining = rtpCooldownEndTime - curServerTickTime;
            if (rtpCooldownRemaining > 0) {
                throw new CommandException(
                    ecText.getText(
                        "cmd.rtp.error.cooldown",
                        TextFormatType.Error,
                        ecText.accent(String.format("%.1f", rtpCooldownRemaining / 20D)))
                );
            }
            // if cooldown has expired
            playerData.setTimeUsedRtp(curServerTickTime);
        }

        new Thread("RTP Location Calculator Thread") {
            public void run() {
                try {
                    {
                        Stopwatch timer = Stopwatch.createStarted();

                        exec(context.getSource(), world);

                        var totalTime = timer.stop();
                        EssentialCommands.LOGGER.info(
                            String.format(
                                "Total RTP Time: %s",
                                totalTime
                            ));
                    }
                } catch (CommandSyntaxException e) {
                    e.printStackTrace();
                }
            }
        }.start();
        return 1;
    }

    private static boolean isValidSpawnPosition(ServerWorld world, int x, int y, int z) {
        // TODO This should be memoized. Cuts exec time in 1/2.
        BlockState targetBlockState = world.getBlockState(new BlockPos(x, y, z));
        BlockState footBlockState = world.getBlockState(new BlockPos(x, y - 1, z));
        return targetBlockState.isAir() && footBlockState.isSolid();
    }

    private static int exec(ServerCommandSource source, ServerWorld world) throws CommandSyntaxException {
        // Position relative to EC spawn locaiton.
        var worldSpawn = ManagerLocator.getInstance().getWorldDataManager().getSpawn();
        if (worldSpawn.isEmpty()) {
            ECText ecText = ECText.access(source.getPlayerOrThrow());
            source.sendError(TextUtil.concat(
                ecText.getText("cmd.rtp.error.pre", TextFormatType.Error),
                ecText.getText("cmd.rtp.error.no_spawn_set", TextFormatType.Error)
            ));
            return -1;
        }
        Vec3i center = worldSpawn.get().intPos();
        return exec(source.getPlayer(), world, center);
    }

    private static final ThreadLocal<Integer> maxY = new ThreadLocal<>();

    private static int exec(ServerPlayerEntity player, ServerWorld world, Vec3i center) {
        var ecText = ECText.access(player);

        int timesRun = 0;
        Optional<BlockPos> pos;
        do {
            timesRun++;
            pos = findRtpPosition(world, center);
        } while (pos.isEmpty() && timesRun <= CONFIG.RTP_MAX_ATTEMPTS);

        if (pos.isEmpty()) {
            return -1;
        }

        // Teleport the player
        PlayerTeleporter.requestTeleport(
            player,
            new MinecraftLocation(world.getRegistryKey(), pos.get(), 0, 0),
            ecText.getText("cmd.rtp.location_name")
        );

        return 1;
    }

    private static Optional<BlockPos> findRtpPosition(ServerWorld world, Vec3i center) {
        maxY.set(world.getHeight()); // TODO: Per-world, preset maximums (or some other mechanism of making this work in the nether)

        // Search for a valid y-level (not in a block, underwater, out of the world, etc.)
        final BlockPos targetXZ = getRandomXZ(center);
        final int x = targetXZ.getX();
        final int z = targetXZ.getZ();
        final Chunk chunk = world.getChunk(targetXZ);
        final int y = getTop(chunk, x, z);

        // This creates an infinite recursive call in the case where all positions on RTP circle are in water.
        //  Addressed by adding timesRun limit.
        if (!isSafePosition(chunk, new BlockPos(x, y - 2, z))) {
            return Optional.empty();
        }

        return Optional.of(new BlockPos(x, y, z));
    }

    private static BlockPos getRandomXZ(Vec3i center) {
        // Calculate position on circle perimeter
        var rand = new Random();
        int r_max = CONFIG.RTP_RADIUS;
        int r_min = CONFIG.RTP_MIN_RADIUS;
        int r = r_max == r_min
            ? r_max
            : rand.nextInt(r_min, r_max);
        final double angle = rand.nextDouble() * 2 * Math.PI;
        final double delta_x = r * Math.cos(angle);
        final double delta_z = r * Math.sin(angle);

        final int new_x = center.getX() + (int) delta_x;
        final int new_z = center.getZ() + (int) delta_z;
        return new BlockPos(new_x, 0, new_z);
    }

    private static boolean isSafePosition(Chunk chunk, BlockPos pos) {
        if (pos.getY() <= chunk.getBottomY()) {
            return false;
        }

        BlockState blockState = chunk.getBlockState(pos);
        return pos.getY() < maxY.get() && blockState.getFluidState().isEmpty() && blockState.getBlock() != Blocks.FIRE;
    }

}
