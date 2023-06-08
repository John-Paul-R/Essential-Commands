package com.fibermc.essentialcommands.commands.helpers;

import com.fibermc.essentialcommands.playerdata.PlayerData;
import com.fibermc.essentialcommands.text.ECText;
import com.fibermc.essentialcommands.text.TextFormatType;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;

public final class FeedbackReceiver implements IFeedbackReceiver {

    private final ServerCommandSource commandSource;

    private FeedbackReceiver(ServerCommandSource commandSource) {
        this.commandSource = commandSource;
    }

    public static IFeedbackReceiver ofSource(ServerCommandSource commandSource) {
        var player = commandSource.getPlayer();
        return player != null
            ? PlayerData.access(player)
            : new FeedbackReceiver(commandSource);
    }

    @Override
    public void sendCommandFeedback(Text text) {
        commandSource.sendFeedback(() -> text, CONFIG.BROADCAST_TO_OPS);
    }

    @Override
    public void sendCommandFeedback(String messageKey, Text... args) {
        sendCommandFeedback(ECText.getInstance().getText(messageKey, TextFormatType.Default, args));
    }

    public void sendCommandError(Text text) {
        commandSource.sendError(text);
    }

    @Override
    public void sendCommandError(String messageKey, Text... args) {
        sendCommandError(ECText.getInstance().getText(messageKey, TextFormatType.Error, args));
    }
}
