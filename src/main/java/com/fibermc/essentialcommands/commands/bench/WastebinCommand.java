package com.fibermc.essentialcommands.commands.bench;

import com.fibermc.essentialcommands.text.ECText;
import com.fibermc.essentialcommands.text.TextFormatType;
import org.jetbrains.annotations.NotNull;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuConstructor;

public class WastebinCommand extends SimpleScreenCommand {
    private static final MenuConstructor SCREEN_HANDLER_FACTORY = (syncId, inventory, player) ->
        ChestMenu.threeRows(syncId, inventory, new SimpleContainer(27));

    @Override
    protected Component getScreenTitle() {
        return ECText.getInstance().getText("cmd.wastebin.name", TextFormatType.Empty);
    }

    @Override
    protected @NotNull MenuConstructor getScreenHandlerFactory() {
        return SCREEN_HANDLER_FACTORY;
    }

    @Override
    protected void onOpen(ServerPlayer player) {

    }
}
