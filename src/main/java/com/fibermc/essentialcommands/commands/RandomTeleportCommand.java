package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.*;
import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.types.MinecraftLocation;
import com.google.common.base.Stopwatch;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.jpcode.eccore.util.TextUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.command.CommandException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.util.Random;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;
import static com.fibermc.essentialcommands.commands.TopCommand.getTop;

/**
 * Heavily referenced from
 * https://github.com/javachaos/randomteleport/blob/master/src/main/java/net.ethermod/commands/RandomTeleportCommand.java
 *
 * Additionally, tons of optimization tips & examples provided by @Wesley1808 on GitHub:
 * https://github.com/Wesley1808/ServerCore/issues/16
 */

public class RandomTeleportCommand implements Command<ServerCommandSource> {

    public RandomTeleportCommand() {}

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity player = context.getSource().getPlayer();
        ServerWorld world = context.getSource().getWorld();

        if (!world.getRegistryKey().equals(World.OVERWORLD)) {
            throw new CommandException(TextUtil.concat(
                ECText.getInstance().getText("cmd.rtp.error.pre").setStyle(CONFIG.FORMATTING_ERROR),
                ECText.getInstance().getText("cmd.rtp.error.not_overworld").setStyle(CONFIG.FORMATTING_ERROR)
            ));
        }

        //TODO Add OP/Permission bypass for RTP cooldown.
        if (CONFIG.RTP_COOLDOWN > 0) {
            ServerCommandSource source = context.getSource();
            int curServerTickTime = source.getServer().getTicks();
            PlayerData playerData = ((ServerPlayerEntityAccess)player).getEcPlayerData();
            var rtpCooldownEndTime = playerData.getTimeUsedRtp() + CONFIG.RTP_COOLDOWN * 20;
            var rtpCooldownRemaining = rtpCooldownEndTime - curServerTickTime;
            if (rtpCooldownRemaining > 0) {
                throw new CommandException(
                    ECText.getInstance().getText(
                        "cmd.rtp.error.cooldown",
                        TextFormatType.Error,
                        Text.literal(String.format("%.1f", rtpCooldownRemaining / 20D)).setStyle(CONFIG.FORMATTING_ACCENT))
                );
            }
            // if cooldown has expired
            playerData.setTimeUsedRtp(curServerTickTime);
        }

        new Thread("RTP Location Calculator Thread") {
            public void run() {
                try {
                    exec(context.getSource(), world);
                } catch (CommandSyntaxException e) {
                    e.printStackTrace();
                }
            }
        }.start();
        return 1;
    }

    private static boolean isValidSpawnPosition(ServerWorld world, double x, double y, double z) {
        // TODO This should be memoized. Cuts exec time in 1/2.
        BlockState targetBlockState = world.getBlockState(new BlockPos(x, y, z));
        BlockState footBlockState = world.getBlockState(new BlockPos(x, y-1, z));
        return targetBlockState.isAir() && footBlockState.getMaterial().isSolid();
    }

    private static int exec(ServerCommandSource source, ServerWorld world) throws CommandSyntaxException {
        // Position relative to EC spawn locaiton.
        MinecraftLocation center = ManagerLocator.getInstance().getWorldDataManager().getSpawn();
        if (center == null) {
            source.sendError(TextUtil.concat(
                ECText.getInstance().getText("cmd.rtp.error.pre"),
                ECText.getInstance().getText("cmd.rtp.error.no_spawn_set")
            ));
            return -1;
        }
        return exec(source.getPlayer(), world, center, 0);
    }

    private static final ThreadLocal<Integer> maxY = new ThreadLocal<>();
    private static int exec(ServerPlayerEntity player, ServerWorld world, MinecraftLocation center, int timesRun) {
        if (timesRun > CONFIG.RTP_MAX_ATTEMPTS) {
            return -1;
        }
        maxY.set(world.getHeight()); // TODO: Per-world, preset maximums (or some other mechanism of making this work in the nether)
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

        final double new_x = center.pos.x + delta_x;
        final double new_z = center.pos.z + delta_z;

        // Search for a valid y-level (not in a block, underwater, out of the world, etc.)
        int new_y;
        final BlockPos targetXZ = new BlockPos(new_x, 0, new_z);

        Chunk chunk = world.getChunk(targetXZ);

        {
            Stopwatch timer = Stopwatch.createStarted();
            new_y = getTop(chunk, (int)new_x, (int)new_z);
            EssentialCommands.LOGGER.info(ECText.getInstance().getText("cmd.rtp.log.location_validate_time", timer.stop()).getString());
        }

        // This creates an infinite recursive call in the case where all positions on RTP circle are in water.
        //  Addressed by adding timesRun limit.
        if (!isSafePosition(chunk, new BlockPos(new_x, new_y-2, new_z))) {
            return exec(player, world, center, timesRun + 1);
        }

        // Teleport the player
        PlayerTeleporter.requestTeleport(
            player,
            new MinecraftLocation(world.getRegistryKey(), new_x, new_y, new_z, 0, 0),
            ECText.getInstance().getText("cmd.rtp.location_name")
        );

        return 1;
    }

    private static boolean isSafePosition(Chunk chunk, BlockPos pos) {
        if (pos.getY() <= chunk.getBottomY()) {
            return false;
        }

        var material = chunk.getBlockState(pos).getMaterial();
        return pos.getY() < maxY.get() && !material.isLiquid() && material != Material.FIRE;
    }

}
