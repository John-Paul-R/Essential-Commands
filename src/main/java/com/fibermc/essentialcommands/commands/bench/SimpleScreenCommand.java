package com.fibermc.essentialcommands.commands.bench;

import com.fibermc.essentialcommands.playerdata.PlayerData;
import org.jetbrains.annotations.NotNull;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.MenuConstructor;

public abstract class SimpleScreenCommand implements Command<CommandSourceStack> {
    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var senderPlayer = context.getSource().getPlayerOrException();
        var senderPlayerData = PlayerData.access(senderPlayer);

        senderPlayer.openMenu(createNamedScreenHandlerFactory());

        senderPlayerData.sendCommandFeedback("cmd.workbench.feedback", getScreenTitle());

        onOpen(senderPlayer);

        return 0;
    }

    protected MenuProvider createNamedScreenHandlerFactory() {
        return new SimpleMenuProvider(getScreenHandlerFactory(), getScreenTitle());
    }

    protected abstract Component getScreenTitle();

    protected abstract @NotNull MenuConstructor getScreenHandlerFactory();

    protected abstract void onOpen(ServerPlayer player);
}
