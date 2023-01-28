package com.fibermc.essentialcommands.commands.bench;

import com.fibermc.essentialcommands.screen.StonecutterCommandScreenHandler;
import org.jetbrains.annotations.NotNull;

import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.ScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class StonecutterCommand extends SimpleScreenCommand {
    private static final ScreenHandlerFactory SCREEN_HANDLER_FACTORY = (syncId, inventory, player) ->
        new StonecutterCommandScreenHandler(
            syncId,
            inventory,
            ScreenHandlerContext.create(player.getEntityWorld(), player.getBlockPos())
        );

    @Override
    protected Text getScreenTitle() {
        return new TranslatableText("block.minecraft.stonecutter");
    }

    @Override
    protected @NotNull ScreenHandlerFactory getScreenHandlerFactory() {
        return SCREEN_HANDLER_FACTORY;
    }

    @Override
    protected void onOpen(ServerPlayerEntity player) {
        player.incrementStat(Stats.INTERACT_WITH_STONECUTTER);
    }
}
