package com.fibermc.essentialcommands.text;

import com.fibermc.essentialcommands.commands.helpers.IFeedbackReceiver;

import com.mojang.brigadier.context.CommandContext;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;

public record Chat(ECText ecText, ServerCommandSource commandSource) implements IFeedbackReceiver {

    public static Chat of(MinecraftServer server) {
        return new Chat(ECText.getInstance(), server.getCommandSource());
    }

    public static Chat of(ServerPlayerEntity player) {
        return new Chat(ECText.access(player), player.getCommandSource());
    }

    public static Chat of(CommandContext<ServerCommandSource> context) {
        return new Chat(ECText.access(context), context.getSource());
    }

    @Override
    public void sendCommandFeedback(Text text) {
        this.commandSource.sendFeedback(() -> text, CONFIG.BROADCAST_TO_OPS);
    }

    @Override
    public void sendCommandFeedback(String messageKey, Text... args) {
        sendCommandFeedback(ecText.getText(messageKey, TextFormatType.Default, args));
    }

    @Override
    public void sendCommandError(Text text) {
        this.commandSource.sendError(text);
    }

    @Override
    public void sendCommandError(String messageKey, Text... args) {
        sendCommandError(this.ecText.getText(messageKey, TextFormatType.Error, args));
    }

    public void sendMessage(String messageKey, Text... args) {
        this.commandSource.sendMessage(this.ecText.getText(messageKey, TextFormatType.Default, args));
    }

    public void sendError(String messageKey, Text... args) {
        this.commandSource.sendMessage(this.ecText.getText(messageKey, TextFormatType.Error, args));
    }
}
