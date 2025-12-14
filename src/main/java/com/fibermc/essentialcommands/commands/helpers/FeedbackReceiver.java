package com.fibermc.essentialcommands.commands.helpers;

import com.fibermc.essentialcommands.playerdata.PlayerData;
import com.fibermc.essentialcommands.text.ECText;
import com.fibermc.essentialcommands.text.TextFormatType;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;

public final class FeedbackReceiver implements IFeedbackReceiver {

    private final CommandSourceStack commandSource;

    private FeedbackReceiver(CommandSourceStack commandSource) {
        this.commandSource = commandSource;
    }

    public static IFeedbackReceiver ofSource(CommandSourceStack commandSource) {
        var player = commandSource.getPlayer();
        return player != null
            ? PlayerData.access(player)
            : new FeedbackReceiver(commandSource);
    }

    @Override
    public void sendCommandFeedback(Component text) {
        commandSource.sendSuccess(() -> text, CONFIG.BROADCAST_TO_OPS);
    }

    @Override
    public void sendCommandFeedback(String messageKey, Component... args) {
        sendCommandFeedback(ECText.getInstance().getText(messageKey, TextFormatType.Default, args));
    }

    public void sendCommandError(Component text) {
        commandSource.sendFailure(text);
    }

    @Override
    public void sendCommandError(String messageKey, Component... args) {
        sendCommandError(ECText.getInstance().getText(messageKey, TextFormatType.Error, args));
    }
}
