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
import com.fibermc.essentialcommands.types.RtpCenter;
import com.google.common.base.Stopwatch;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

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
public class RandomTeleportCommand implements Command<CommandSourceStack> {

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
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        ServerLevel world = context.getSource().getLevel();
        var ecText = ECText.access(player);
        if (!CONFIG.RTP_ENABLED_WORLDS.contains(world.dimension())) {
            var currentWorldAsText = Component.nullToEmpty(world.dimension().identifier().toString());
            PlayerData.access(player).sendCommandError(TextUtil.concat(
                ecText.getText("cmd.rtp.error.pre", TextFormatType.Error),
                ecText.getText("cmd.rtp.error.world_not_enabled", TextFormatType.Error, currentWorldAsText)
            ));
            return 0;
        }

        if (CONFIG.RTP_COOLDOWN > 0 && !ECPerms.check(context.getSource(), ECPerms.Registry.bypass_randomteleport_cooldown)) {
            int curServerTickTime = context.getSource().getServer().getTickCount();
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
            EssentialCommands.LOGGER.info("Starting RTP location search for {}", player.getGameProfile().name());

            Stopwatch timer = Stopwatch.createStarted();

            exec(player, world);

            var totalTime = timer.stop();
            EssentialCommands.LOGGER.info("Total RTP Time: {}", totalTime);
        });

        return SINGLE_SUCCESS;
    }

    final static class ExecutionContext {
        public final int topY;
        public final int bottomY;

        public ExecutionContext(ServerLevel world) {
            this.topY = world.getMaxY();
            this.bottomY = world.getMinY();
        }
    }

    private static void exec(ServerPlayer player, ServerLevel world) {
        var centerOpt = getRtpCenter(player);
        if (centerOpt.isEmpty()) {
            return;
        }
        Vec3i center = centerOpt.get();

        final var executionContext = new ExecutionContext(world);
        final var heightFinder = HeightFindingStrategy.forWorld(world.dimension());

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
            new MinecraftLocation(world.dimension(), pos.get(), 0, 0),
            ECText.access(player).getText("cmd.rtp.location_name")
        );
    }

    private static Optional<Vec3i> getRtpCenter(ServerPlayer player) {
        var configuredRtpCenter = CONFIG.RTP_CENTER.getPosition();
        if (configuredRtpCenter.isPresent()) {
            var pair = configuredRtpCenter.get();
            return Optional.of(new Vec3i(pair.x(), 0, pair.z()));
        }

        if (CONFIG.RTP_CENTER instanceof RtpCenter.Spawn) {
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

        EssentialCommands.LOGGER.warn("Received no-value (not even default) for RTP_CENTER. (should be 'SPAWN'?)");
        return Optional.empty();
    }

    private static Optional<BlockPos> findRtpPosition(ServerLevel world, Vec3i center, HeightFinder heightFinder, ExecutionContext ctx) {
        // Search for a valid y-level (not in a block, underwater, out of the world, etc.)
        final BlockPos targetXZ = getRandomXZ(center);
        final ChunkAccess chunk = world.getChunk(targetXZ);

        for (BlockPos.MutableBlockPos candidateBlock : getChunkCandidateBlocks(chunk.getPos())) {
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

    private static boolean isSafePosition(ChunkAccess chunk, BlockPos pos, ExecutionContext ctx) {
        if (pos.getY() <= chunk.getMinY()) {
            return false;
        }

        BlockState blockState = chunk.getBlockState(pos);
        return pos.getY() < ctx.topY && blockState.getFluidState().isEmpty() && blockState.getBlock() != Blocks.FIRE;
    }

    public static Iterable<BlockPos.MutableBlockPos> getChunkCandidateBlocks(ChunkPos chunkPos) {
        return () -> new Iterator<>() {
            private int _idx = -1;
            private final BlockPos.MutableBlockPos _pos = new BlockPos.MutableBlockPos();

            @Override
            public boolean hasNext() {
                return _idx < 4;
            }

            @Override
            public BlockPos.MutableBlockPos next() {
                _idx++;
                return switch (_idx) {
                    case 0 -> _pos.set(chunkPos.getMinBlockX(), 0, chunkPos.getMinBlockZ());
                    case 1 -> _pos.set(chunkPos.getMinBlockX(), 0, chunkPos.getMaxBlockZ());
                    case 2 -> _pos.set(chunkPos.getMaxBlockX(), 0, chunkPos.getMinBlockZ());
                    case 3 -> _pos.set(chunkPos.getMaxBlockX(), 0, chunkPos.getMaxBlockZ());
                    case 4 -> _pos.set(chunkPos.getMiddleBlockX(), 0, chunkPos.getMiddleBlockZ());
                    default -> throw new IllegalStateException("Unexpected value: " + _idx);
                };
            }
        };
    }

}
