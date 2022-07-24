package com.fibermc.essentialcommands.commands.bench;

import com.fibermc.essentialcommands.ECText;
import com.fibermc.essentialcommands.TextFormatType;
import org.jetbrains.annotations.NotNull;

import net.minecraft.inventory.SimpleInventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class WastebinCommand extends SimpleScreenCommand {
    private static final ScreenHandlerFactory SCREEN_HANDLER_FACTORY = (syncId, inventory, player) ->
        GenericContainerScreenHandler.createGeneric9x3(syncId, inventory, new SimpleInventory(27));

    @Override
    protected Text getScreenTitle() {
        return ECText.getInstance().getText("cmd.wastebin.name", TextFormatType.Empty);
    }

    @Override
    protected @NotNull ScreenHandlerFactory getScreenHandlerFactory() {
        return SCREEN_HANDLER_FACTORY;
    }

    @Override
    protected void onOpen(ServerPlayerEntity player) {

    }
}
