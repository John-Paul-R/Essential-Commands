package com.fibermc.essentialcommands.commands.bench;

import com.fibermc.essentialcommands.ECText;
import com.fibermc.essentialcommands.TextFormatType;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;

public class WastebinCommand extends SimpleScreenCommand {
    private static final ScreenHandlerFactory screenHandlerFactory = (syncId, inventory, player) ->
        GenericContainerScreenHandler.createGeneric9x3(syncId, inventory, new SimpleInventory(27));

    @Override
    protected Text getScreenTitle() {
        return ECText.getInstance().getText("cmd.wastebin.name", TextFormatType.Empty);
    }

    @Override
    protected @NotNull ScreenHandlerFactory getScreenHandlerFactory() {
        return screenHandlerFactory;
    }

    @Override
    protected void onOpen(ServerPlayerEntity player) {

    }
}
