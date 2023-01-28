package com.fibermc.essentialcommands.text;

import com.fibermc.essentialcommands.commands.CommandUtil;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import dev.jpcode.eccore.util.TextUtil;

public final class ChatConfirmationPrompt {

    private final ServerPlayerEntity player;
    private final MutableText text;

    public ChatConfirmationPrompt(CommandContext<ServerCommandSource> context,
                                  CommandNode<ServerCommandSource> confirmCommand,
                                  CommandNode<ServerCommandSource> denyCommand,
                                  MutableText confirmText,
                                  MutableText denyText) throws CommandSyntaxException {
        this(
            context.getSource().getPlayer(),
            CommandUtil.getCommandString(context.getSource(), confirmCommand),
            CommandUtil.getCommandString(context.getSource(), denyCommand),
            confirmText,
            denyText
        );
    }

    public ChatConfirmationPrompt(ServerPlayerEntity player,
                                  String confirmCommandStr,
                                  String denyCommandStr,
                                  MutableText confirmText,
                                  MutableText denyText) {
        this.player = player;
        this.text = (MutableText) TextUtil.spaceBetween(
            new Text[]{
                confirmText.setStyle(
                    confirmText.getStyle().withClickEvent(new ClickEvent(
                        ClickEvent.Action.RUN_COMMAND,
                        confirmCommandStr))),
                denyText.setStyle(
                    denyText.getStyle().withClickEvent(new ClickEvent(
                        ClickEvent.Action.RUN_COMMAND,
                        denyCommandStr))),
            },
            64,
            14
        );
    }

    public void send() {
        this.player.sendMessage(this.text);
    }

    public MutableText getText() {
        return this.text;
    }

}
