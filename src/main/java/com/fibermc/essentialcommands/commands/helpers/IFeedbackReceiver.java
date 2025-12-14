package com.fibermc.essentialcommands.commands.helpers;

import net.minecraft.network.chat.Component;

public interface IFeedbackReceiver {
    void sendCommandFeedback(Component text);

    void sendCommandFeedback(String messageKey, Component... args);

    void sendCommandError(Component text);

    void sendCommandError(String messageKey, Component... args);
}
