package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.*;
import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.types.MinecraftLocation;
import com.fibermc.essentialcommands.util.TextUtil;
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

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;

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
                try {
                    exec(context.getSource(), world);
                } catch (CommandSyntaxException e) {
                    e.printStackTrace();
                }
            }
        };

        //TODO Add OP/Permission bypass for RTP cooldown.
        if (CONFIG.RTP_COOLDOWN.getValue() > 0) {
            ServerCommandSource source = context.getSource();
            int ticks = source.getServer().getTicks();
            PlayerData playerData = ((ServerPlayerEntityAccess)player).getEcPlayerData();
            // if cooldown has expired
            if (playerData.getRtpNextUsableTime() < ticks) {
                playerData.setRtpNextUsableTime(ticks + CONFIG.RTP_COOLDOWN.getValue()*20);
                thread.start();
                resultCode = 1;
            } else {
                source.sendError(TextUtil.concat(
                    ECText.getInstance().getText("cmd.rtp.error.cooldown.1").setStyle(CONFIG.FORMATTING_ERROR.getValue()),
                    new LiteralText(String.format("%.1f", (playerData.getRtpNextUsableTime() - ticks)/20D)).setStyle(CONFIG.FORMATTING_ACCENT.getValue()),
                    ECText.getInstance().getText("cmd.rtp.error.cooldown.2").setStyle(CONFIG.FORMATTING_ERROR.getValue())
                ));
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

    private static int exec(ServerPlayerEntity player, ServerWorld world, MinecraftLocation center, int timesRun) {
        if (timesRun > CONFIG.RTP_MAX_ATTEMPTS.getValue()) {
            return -1;
        }
        // Calculate position on circle perimeter
        int r = CONFIG.RTP_RADIUS.getValue();
        double angle = (new Random()).nextDouble()*2*Math.PI;
        double delta_x = r * Math.cos(angle);
        double delta_z = r * Math.sin(angle);

        double new_x = center.pos.x + delta_x;
        double new_z = center.pos.z + delta_z;

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

        EssentialCommands.LOGGER.info(ECText.getInstance().getText("cmd.rtp.log.location_validate_time", timer.stop()).getString());

        // This creates an infinite recursive call in the case where all positions on RTP circle are in water.
        //  Addressed by adding timesRun limit.
        if (world.isWater(new BlockPos(new_x, new_y-2, new_z))) {
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

}
