package com.fibermc.essentialcommands.commands;

import java.util.Iterator;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.fibermc.essentialcommands.ECPerms;
import com.fibermc.essentialcommands.EssentialCommands;
import com.fibermc.essentialcommands.ManagerLocator;
import com.fibermc.essentialcommands.commands.helpers.HeightFinder;
import com.fibermc.essentialcommands.commands.helpers.HeightFindingStrategy;
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
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.chunk.Chunk;

import dev.jpcode.eccore.util.TextUtil;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;

/*
 * Heavily referenced from
 * https://github.com/javachaos/randomteleport/blob/master/src/main/java/net.ethermod/commands/RandomTeleportCommand.java
 *
 * Additionally, tons of optimization tips & examples provided by @Wesley1808 on GitHub:
 * https://github.com/Wesley1808/ServerCore/issues/16
 *
 */
@SuppressWarnings("checkstyle:all")
public class RandomTeleportCommand implements Command<ServerCommandSource> {

    public RandomTeleportCommand() {}

    private final Thread.UncaughtExceptionHandler exceptionHandler = (thread, throwable) -> {
        EssentialCommands.LOGGER.error("Exception in RTP calculator thread", throwable);
    };
    private final Executor threadExecutor = Executors.newCachedThreadPool(runnable -> {
        var thread = new Thread(runnable, "RTP Location Calculator Thread");

        thread.setUncaughtExceptionHandler(exceptionHandler);

        return thread;
    });

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayerOrThrow();
        ServerWorld world = context.getSource().getWorld();
        var ecText = ECText.access(player);
        if (!CONFIG.RTP_ENABLED_WORLDS.contains(world.getRegistryKey())) {
            var currentWorldAsText = Text.of(world.getRegistryKey().getValue().toString());
            PlayerData.access(player).sendCommandError(TextUtil.concat(
                ecText.getText("cmd.rtp.error.pre", TextFormatType.Error),
                ecText.getText("cmd.rtp.error.world_not_enabled", TextFormatType.Error, currentWorldAsText)
            ));
            return 0;
        }

        if (CONFIG.RTP_COOLDOWN > 0 && !ECPerms.check(context.getSource(), ECPerms.Registry.bypass_randomteleport_cooldown)) {
            int curServerTickTime = context.getSource().getServer().getTicks();
            var playerData = PlayerData.access(player);
            var rtpCooldownEndTime = playerData.getTimeUsedRtp() + CONFIG.RTP_COOLDOWN * 20;
            var rtpCooldownRemaining = rtpCooldownEndTime - curServerTickTime;
            if (rtpCooldownRemaining > 0) {
                    playerData.sendError(
                        "cmd.rtp.error.cooldown",
                        ecText.accent(String.format("%.1f", rtpCooldownRemaining / 20D))
                    );
                    return 0;
            }
            // if cooldown has expired
            playerData.setTimeUsedRtp(curServerTickTime);
        }

        threadExecutor.execute(() -> {
            EssentialCommands.LOGGER.info(
                String.format(
                    "Starting RTP location search for %s",
                    player.getGameProfile().getName()
                ));

            Stopwatch timer = Stopwatch.createStarted();

            exec(player, world);

            var totalTime = timer.stop();
            EssentialCommands.LOGGER.info(
                String.format(
                    "Total RTP Time: %s",
                    totalTime
                ));
        });

        return SINGLE_SUCCESS;
    }

    final static class ExecutionContext {
        public final int topY;
        public final int bottomY;

        public ExecutionContext(ServerWorld world) {
            this.topY = world.getTopY();
            this.bottomY = world.getBottomY();
        }
    }

    private static void exec(ServerPlayerEntity player, ServerWorld world) {
        var centerOpt = getRtpCenter(player);
        if (centerOpt.isEmpty()) {
            return;
        }
        Vec3i center = centerOpt.get();

        final var executionContext = new ExecutionContext(world);
        final var heightFinder = HeightFindingStrategy.forWorld(world.getRegistryKey());

        int timesRun = 0;
        Optional<BlockPos> pos;
        do {
            timesRun++;
            pos = findRtpPosition(world, center, heightFinder, executionContext);
        } while (pos.isEmpty() && timesRun <= CONFIG.RTP_MAX_ATTEMPTS);

        if (pos.isEmpty()) {
            return;
        }

        // Teleport the player
        PlayerTeleporter.requestTeleport(
            player,
            new MinecraftLocation(world.getRegistryKey(), pos.get(), 0, 0),
            ECText.access(player).getText("cmd.rtp.location_name")
        );
    }

    private static Optional<Vec3i> getRtpCenter(ServerPlayerEntity player) {
        // Position relative to EC spawn locaiton.
        var worldSpawn = ManagerLocator.getInstance().getWorldDataManager().getSpawn();
        if (worldSpawn.isEmpty()) {
            var ecText = ECText.access(player);
            PlayerData.access(player).sendCommandError(TextUtil.concat(
                ecText.getText("cmd.rtp.error.pre", TextFormatType.Error),
                ecText.getText("cmd.rtp.error.no_spawn_set", TextFormatType.Error)
            ));
            return Optional.empty();
        }

        return Optional.of(worldSpawn.get().intPos());
    }

    private static Optional<BlockPos> findRtpPosition(ServerWorld world, Vec3i center, HeightFinder heightFinder, ExecutionContext ctx) {
        // Search for a valid y-level (not in a block, underwater, out of the world, etc.)
        final BlockPos targetXZ = getRandomXZ(center);
        final Chunk chunk = world.getChunk(targetXZ);

        for (BlockPos.Mutable candidateBlock : getChunkCandidateBlocks(chunk.getPos())) {
            final int x = candidateBlock.getX();
            final int z = candidateBlock.getZ();
            final OptionalInt yOpt = heightFinder.getY(chunk, x, z);
            if (yOpt.isEmpty()) {
                continue;
            }
            final int y = yOpt.getAsInt();

            if (isSafePosition(chunk, new BlockPos(x, y - 2, z), ctx)) {
                return Optional.of(new BlockPos(x, y, z));
            }
        }

        // This creates an infinite recursive call in the case where all positions on RTP circle are in water.
        //  Addressed by adding timesRun limit.
        return Optional.empty();
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

    private static boolean isSafePosition(Chunk chunk, BlockPos pos, ExecutionContext ctx) {
        if (pos.getY() <= chunk.getBottomY()) {
            return false;
        }

        BlockState blockState = chunk.getBlockState(pos);
        return pos.getY() < ctx.topY && blockState.getFluidState().isEmpty() && blockState.getBlock() != Blocks.FIRE;
    }

    public static Iterable<BlockPos.Mutable> getChunkCandidateBlocks(ChunkPos chunkPos) {
        return () -> new Iterator<>() {
            private int _idx = -1;
            private final BlockPos.Mutable _pos = new BlockPos.Mutable();

            @Override
            public boolean hasNext() {
                return _idx < 4;
            }

            @Override
            public BlockPos.Mutable next() {
                _idx++;
                return switch (_idx) {
                    case 0 -> _pos.set(chunkPos.getStartX(), 0, chunkPos.getStartZ());
                    case 1 -> _pos.set(chunkPos.getStartX(), 0, chunkPos.getEndZ());
                    case 2 -> _pos.set(chunkPos.getEndX(), 0, chunkPos.getStartZ());
                    case 3 -> _pos.set(chunkPos.getEndX(), 0, chunkPos.getEndZ());
                    case 4 -> _pos.set(chunkPos.getCenterX(), 0, chunkPos.getCenterZ());
                    default -> throw new IllegalStateException("Unexpected value: " + _idx);
                };
            }
        };
    }

}
