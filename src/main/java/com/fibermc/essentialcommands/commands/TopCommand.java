package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.ECText;
import com.fibermc.essentialcommands.PlayerTeleporter;
import com.fibermc.essentialcommands.types.MinecraftLocation;
import com.google.common.base.Stopwatch;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

public class TopCommand implements Command<ServerCommandSource> {
    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();
        World world = source.getWorld();
        Vec3d playerPos = player.getPos();

        int new_y;
        double new_x = playerPos.x;
        double new_z = playerPos.z;

        Stopwatch timer = Stopwatch.createStarted();
        BlockHitResult blockHitResult = world.raycast(new RaycastContext(
                new Vec3d(new_x, world.getHeight(), new_z),
                new Vec3d(new_x, 1, new_z),
                RaycastContext.ShapeType.COLLIDER,
                RaycastContext.FluidHandling.SOURCE_ONLY,
                player
        ));
        new_y = blockHitResult.getBlockPos().getY() + 1;

        // Teleport the player
        PlayerTeleporter.requestTeleport(
                player,
                new MinecraftLocation(world.getRegistryKey(), new_x, new_y, new_z, player.getHeadYaw(), player.pitch),
                ECText.getInstance().getText("cmd.top.location_name")
        );

        return 0;

    }


}
