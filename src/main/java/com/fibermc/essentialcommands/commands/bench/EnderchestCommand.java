package com.fibermc.essentialcommands.commands.bench;

import org.jetbrains.annotations.NotNull;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ChestMenu;

public class EnderchestCommand extends SimpleScreenCommand {
    @Override
    protected Component getScreenTitle() {
        return Component.translatable("container.enderchest");
    }

    @Override
    protected @NotNull MenuProvider getScreenHandlerFactory() {
        return new SimpleMenuProvider(
            (syncId, inventory, player) ->
                ChestMenu.threeRows(syncId, inventory, player.getEnderChestInventory()),
            Component.translatable("container.enderchest")
        );
    }

    @Override
    protected void onOpen(ServerPlayer player) {
        player.awardStat(Stats.OPEN_ENDERCHEST);
    }
}
