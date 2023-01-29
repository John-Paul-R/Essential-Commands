package com.fibermc.essentialcommands.commands.helpers;

import net.minecraft.text.Text;

public interface IFeedbackReceiver {
    void sendCommandFeedback(Text text);

    void sendCommandFeedback(String messageKey, Text... args);

    void sendCommandError(Text text);

    void sendCommandError(String messageKey, Text... args);
}
