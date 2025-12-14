package com.fibermc.essentialcommands.text;

import com.fibermc.essentialcommands.commands.CommandUtil;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;

import dev.jpcode.eccore.util.TextUtil;

public final class ChatConfirmationPrompt {

    private final ServerPlayer player;
    private final MutableComponent text;

    public ChatConfirmationPrompt(CommandContext<CommandSourceStack> context,
                                  CommandNode<CommandSourceStack> confirmCommand,
                                  CommandNode<CommandSourceStack> denyCommand,
                                  MutableComponent confirmText,
                                  MutableComponent denyText) throws CommandSyntaxException {
        this(
            context.getSource().getPlayer(),
            CommandUtil.getCommandString(context.getSource(), confirmCommand),
            CommandUtil.getCommandString(context.getSource(), denyCommand),
            confirmText,
            denyText
        );
    }

    public ChatConfirmationPrompt(ServerPlayer player,
                                  String confirmCommandStr,
                                  String denyCommandStr,
                                  MutableComponent confirmText,
                                  MutableComponent denyText) {
        this.player = player;
        this.text = TextUtil.spaceBetween(
            new Component[]{
                confirmText.setStyle(
                    confirmText.getStyle().withClickEvent(new ClickEvent.RunCommand(
                        confirmCommandStr))),
                denyText.setStyle(
                    denyText.getStyle().withClickEvent(new ClickEvent.RunCommand(
                        denyCommandStr))),
            },
            64,
            14
        );
    }

    public ChatConfirmationPrompt(ServerPlayer player,
                                  String commandStr,
                                  MutableComponent text) {
        this.player = player;
        this.text = Component.literal(" ".repeat(15)).append(
            text.setStyle(text.getStyle().withClickEvent(
                new ClickEvent.RunCommand(commandStr))));
    }

    public void send() {
        this.player.sendSystemMessage(this.text);
    }

    public MutableComponent getText() {
        return this.text;
    }

}
