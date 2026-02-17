package com.fibermc.essentialcommands.commands.bench;

import com.fibermc.essentialcommands.EssentialCommands;
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
            (syncId, inventory, player) -> {
                var echestInventory = player.getEnderChestInventory();
                if (echestInventory.getContainerSize() == 9 * 3) {
                    return ChestMenu.threeRows(syncId, inventory, echestInventory);
                }
                if (echestInventory.getContainerSize() == 9 * 6) {
                    return ChestMenu.sixRows(syncId, inventory, echestInventory);
                }
                EssentialCommands.LOGGER.warn(
                    "Expected enderchest to be 9x3 or 9x6 (modded), but found size '{}'."
                        + " This is unsupported (If this is an intended configuration, report"
                        + " this along with your log file / mod list at https://github.com/John-Paul-R/Essential-Commands/issues)",
                    echestInventory.getContainerSize());
                return ChestMenu.threeRows(syncId, inventory, echestInventory);
            },
            Component.translatable("container.enderchest")
        );
    }

    @Override
    protected void onOpen(ServerPlayer player) {
        player.awardStat(Stats.OPEN_ENDERCHEST);
    }
}
