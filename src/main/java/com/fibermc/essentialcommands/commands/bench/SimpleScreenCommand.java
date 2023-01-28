package com.fibermc.essentialcommands.commands.bench;

import com.fibermc.essentialcommands.playerdata.PlayerData;
import org.jetbrains.annotations.NotNull;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerFactory;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public abstract class SimpleScreenCommand implements Command<ServerCommandSource> {
    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var senderPlayer = context.getSource().getPlayer();
        var senderPlayerData = PlayerData.access(senderPlayer);

        senderPlayer.openHandledScreen(createNamedScreenHandlerFactory());

        senderPlayerData.sendCommandFeedback("cmd.workbench.feedback", getScreenTitle());

        onOpen(senderPlayer);

        return 0;
    }

    protected NamedScreenHandlerFactory createNamedScreenHandlerFactory() {
        return new SimpleNamedScreenHandlerFactory(getScreenHandlerFactory(), getScreenTitle());
    }

    protected abstract Text getScreenTitle();

    protected abstract @NotNull ScreenHandlerFactory getScreenHandlerFactory();

    protected abstract void onOpen(ServerPlayerEntity player);
}
