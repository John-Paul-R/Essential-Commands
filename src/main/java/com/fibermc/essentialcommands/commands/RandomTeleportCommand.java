package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.*;
import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.types.MinecraftLocation;
import com.google.common.base.Stopwatch;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.BlockState;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.gradle.internal.impldep.org.apache.maven.settings.Server;

import java.util.Random;
import java.util.UUID;

/**
 * Heavily referenced from
 * https://github.com/javachaos/randomteleport/blob/master/src/main/java/net.ethermod/commands/RandomTeleportCommand.java
 */

public class RandomTeleportCommand implements Command<ServerCommandSource> {

    public RandomTeleportCommand() {}

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        int resultCode = -1;

        ServerPlayerEntity player = context.getSource().getPlayer();
        ServerWorld world = context.getSource().getWorld();

        Thread thread = new Thread("RTP Location Calculator Thread") {
            public void run() {
                exec(player, world);
            }
        };

        //TODO Add OP/Permission bypass for RTP cooldown.
        if (Config.RTP_COOLDOWN > 0) {
            ServerCommandSource source = context.getSource();
            int ticks = source.getMinecraftServer().getTicks();
            PlayerData playerData = ((ServerPlayerEntityAccess)player).getEcPlayerData();
            // if cooldown has expired
            if (playerData.getRtpNextUsableTime() < ticks) {
                playerData.setRtpNextUsableTime(ticks + Config.RTP_COOLDOWN*20);
                thread.start();
                resultCode = 1;
            } else {
                source.sendError(new LiteralText("")
                    .append(new LiteralText("Could not execute command `/rtp`. Reason: command is on cooldown. (").setStyle(Config.FORMATTING_ERROR))
                    .append(new LiteralText(String.format("%.1f", (playerData.getRtpNextUsableTime() - ticks)/20D)).setStyle(Config.FORMATTING_ACCENT))
                    .append(new LiteralText(" seconds remaining.)").setStyle(Config.FORMATTING_ERROR))
                );
                resultCode = -2;
            }
        } else {
            thread.start();
            resultCode = 1;
        }

        return resultCode;
    }

    private static boolean isValidSpawnPosition(ServerWorld world, double x, double y, double z) {
        // TODO This should be memoized. Cuts exec time in 1/2.
        BlockState targetBlockState = world.getBlockState(new BlockPos(x, y, z));
        BlockState footBlockState = world.getBlockState(new BlockPos(x, y-1, z));
        return targetBlockState.isAir() && footBlockState.getMaterial().isSolid();
    }

    private static int exec(ServerPlayerEntity playerEntity, ServerWorld world) {
        return exec(playerEntity, world, 0);
    }

    private static int exec(ServerPlayerEntity player, ServerWorld world, int timesRun) {
        if (timesRun > Config.RTP_MAX_ATTEMPTS) {
            return -1;
        }
        // Calculate position on circle perimeter
        int r = Config.RTP_RADIUS;
        double angle = (new Random()).nextDouble()*2*Math.PI;
        double delta_x = r * Math.cos(angle);
        double delta_z = r * Math.sin(angle);
        // Position relative to EC spawn locaiton.
        MinecraftLocation spawnLocation = ManagerLocator.INSTANCE.getWorldDataManager().getSpawn();
        double new_x = spawnLocation.pos.x + delta_x;
        double new_z = spawnLocation.pos.z + delta_z;

        // Search for a valid y-level (not in a block, underwater, out of the world, etc.)
        // TODO Can maybe run a loop that checks every-other block? (player is 2 blocks high)
        int new_y;
        Stopwatch timer = Stopwatch.createStarted();
        BlockHitResult blockHitResult = world.raycast(new RaycastContext(
            new Vec3d(new_x, world.getHeight(), new_z),
            new Vec3d(new_x, 1, new_z),
            RaycastContext.ShapeType.COLLIDER,
            RaycastContext.FluidHandling.SOURCE_ONLY,
            player
        ));
        new_y = blockHitResult.getBlockPos().getY() + 1;

        EssentialCommands.LOGGER.info("Time taken to calculate if RTP location is valid: " + timer.stop());

        // This creates an infinite recursive call in the case where all positions on RTP circle are in water.
        //  Addressed by adding timesRun limit.
        if (world.isWater(new BlockPos(new_x, new_y-2, new_z))) {
            return exec(player, world, timesRun + 1);
        }

        // Teleport the player
        PlayerTeleporter.requestTeleport(
            player,
            new MinecraftLocation(world.getRegistryKey(), new_x, new_y, new_z, 0, 0),
            "random location"
        );

        return 1;
    }

}
