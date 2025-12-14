package com.fibermc.essentialcommands.commands.bench;

import com.fibermc.essentialcommands.screen.StonecutterCommandScreenHandler;
import org.jetbrains.annotations.NotNull;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuConstructor;

public class StonecutterCommand extends SimpleScreenCommand {
    private static final MenuConstructor SCREEN_HANDLER_FACTORY = (syncId, inventory, player) ->
        new StonecutterCommandScreenHandler(
            syncId,
            inventory,
            ContainerLevelAccess.create(player.level(), player.blockPosition())
        );

    @Override
    protected Component getScreenTitle() {
        return Component.translatable("block.minecraft.stonecutter");
    }

    @Override
    protected @NotNull MenuConstructor getScreenHandlerFactory() {
        return SCREEN_HANDLER_FACTORY;
    }

    @Override
    protected void onOpen(ServerPlayer player) {
        player.awardStat(Stats.INTERACT_WITH_STONECUTTER);
    }
}
