package com.fibermc.essentialcommands.commands.bench;

import org.jetbrains.annotations.NotNull;

import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;

public class EnderchestCommand extends SimpleScreenCommand {
    @Override
    protected Text getScreenTitle() {
        return Text.translatable("container.enderchest");
    }

    @Override
    protected @NotNull NamedScreenHandlerFactory getScreenHandlerFactory() {
        return new SimpleNamedScreenHandlerFactory(
            (syncId, inventory, player) ->
                GenericContainerScreenHandler.createGeneric9x3(syncId, inventory, player.getEnderChestInventory()),
            Text.translatable("container.enderchest")
        );
    }

    @Override
    protected void onOpen(ServerPlayerEntity player) {
        player.incrementStat(Stats.OPEN_ENDERCHEST);
    }
}
