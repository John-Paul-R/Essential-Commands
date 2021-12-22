package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.ECText;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.screen.*;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;

public class WastebinCommand implements Command<ServerCommandSource> {

    public WastebinCommand() {}

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity senderPlayer = source.getPlayer();

        senderPlayer.openHandledScreen(createScreenHandlerFactory(senderPlayer.getEntityWorld(), senderPlayer.getBlockPos()));

        source.sendFeedback(
            new LiteralText("Opened wastebin.").setStyle(CONFIG.FORMATTING_DEFAULT.getValue()),
            CONFIG.BROADCAST_TO_OPS.getValue()
        );

        return 0;
    }

    private NamedScreenHandlerFactory createScreenHandlerFactory(World world, BlockPos pos) {
        return new SimpleNamedScreenHandlerFactory((syncId, inventory, player) -> {
            return GenericContainerScreenHandler.createGeneric9x3(syncId, inventory, new SimpleInventory(27));
        }, ECText.getInstance().getText("cmd.wastebin.container_ui_name"));
    }

}
