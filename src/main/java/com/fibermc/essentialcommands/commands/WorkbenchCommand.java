package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.Config;
import com.fibermc.essentialcommands.screen.CraftingCommandScreenHandler;
import com.fibermc.essentialcommands.util.TextUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.s2c.play.CraftFailedResponseS2CPacket;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import net.minecraft.screen.*;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.LiteralText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WorkbenchCommand implements Command<ServerCommandSource> {

    public WorkbenchCommand() {
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity senderPlayer = source.getPlayer();

        senderPlayer.openHandledScreen(createScreenHandlerFactory(senderPlayer.getEntityWorld(), senderPlayer.getBlockPos()));
        senderPlayer.incrementStat(Stats.INTERACT_WITH_CRAFTING_TABLE);

        source.sendFeedback(
            new LiteralText("Opened workbench. ").setStyle(Config.FORMATTING_DEFAULT),
            Config.BROADCAST_TO_OPS
        );

        return 0;
    }

    private NamedScreenHandlerFactory createScreenHandlerFactory(World world, BlockPos pos) {
        return new SimpleNamedScreenHandlerFactory((syncId, inventory, player) -> {
            return new CraftingCommandScreenHandler(syncId, inventory, ScreenHandlerContext.create(world, pos));
        }, new LiteralText("EC Workbench"));

    }

}
