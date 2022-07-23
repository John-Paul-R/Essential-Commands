package com.fibermc.essentialcommands.commands.bench;

import com.fibermc.essentialcommands.screen.StonecutterCommandScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class StonecutterCommand extends SimpleScreenCommand {
    private static final ScreenHandlerFactory screenHandlerFactory = (syncId, inventory, player) ->
        new StonecutterCommandScreenHandler(
            syncId,
            inventory,
            ScreenHandlerContext.create(player.getEntityWorld(), player.getBlockPos())
        );

    @Override
    protected Text getScreenTitle() {
        return Text.translatable("block.minecraft.stonecutter");
    }

    @Override
    protected @NotNull ScreenHandlerFactory getScreenHandlerFactory() {
        return screenHandlerFactory;
    }

    @Override
    protected void onOpen(ServerPlayerEntity player) {
        player.incrementStat(Stats.INTERACT_WITH_STONECUTTER);
    }
}
