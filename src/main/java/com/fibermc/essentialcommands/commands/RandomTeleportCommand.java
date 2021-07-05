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
        //TODO Add OP/Permission bypass for RTP cooldown.
        if (Config.RTP_COOLDOWN > 0) {
            ServerCommandSource source = context.getSource();
            int ticks = source.getMinecraftServer().getTicks();
            PlayerData playerData = ((ServerPlayerEntityAccess)source.getPlayer()).getEcPlayerData();
            // if cooldown has expired
            if (playerData.getRtpNextUsableTime() < ticks) {
                playerData.setRtpNextUsableTime(ticks + Config.RTP_COOLDOWN*20);
                exec(context);
            } else {
                source.getPlayer().sendSystemMessage(
                    new LiteralText("")
                        .append(new LiteralText("Could not execute command `/rtp`. Reason: command is on cooldown. (").setStyle(Config.FORMATTING_ERROR))
                        .append(new LiteralText(String.format("%.1f", (playerData.getRtpNextUsableTime() - ticks)/20D)).setStyle(Config.FORMATTING_ACCENT))
                        .append(new LiteralText(" seconds remaining.)").setStyle(Config.FORMATTING_ERROR)),
                    new UUID(0,0)
                );
            }
        } else {
            exec(context);
        }


        return 0;
    }

    private static boolean isValidSpawnPosition(ServerWorld world, double x, double y, double z) {

        BlockState targetBlockState = world.getBlockState(new BlockPos(x, y, z));
        BlockState footBlockState = world.getBlockState(new BlockPos(x, y-1, z));
        return targetBlockState.isAir() && footBlockState.getMaterial().isSolid();
    }

    private static int exec(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        ServerPlayerEntity player = ctx.getSource().getPlayer();
        ServerWorld world = ctx.getSource().getWorld();
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
        if (world.isWater(new BlockPos(new_x, new_y-2, new_z))) {
            return exec(ctx);
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
