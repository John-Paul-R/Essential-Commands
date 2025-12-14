package com.fibermc.essentialcommands.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.server.level.ServerPlayer;

import dev.jpcode.eccore.util.TextUtil;

public final class CommandUtil {

    private CommandUtil() {}

    public static RequiredArgumentBuilder<CommandSourceStack, EntitySelector> targetPlayerArgument() {
        return Commands.argument("target_player", EntityArgument.player());
    }

    public static String getCommandString(CommandSourceStack source, CommandNode<CommandSourceStack> commandNode) {
        CommandDispatcher<CommandSourceStack> dispatcher = source.getServer().getCommands().getDispatcher();

        return "/" + TextUtil.joinStrings(
            dispatcher.getPath(commandNode),
            CommandDispatcher.ARGUMENT_SEPARATOR
        );
    }

    public static CommandSyntaxException createSimpleException(Message msg) {
        return new CommandSyntaxException(new SimpleCommandExceptionType(msg), msg);
    }

    public static ServerPlayer getCommandTargetPlayer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        try {
            return EntityArgument.getPlayer(context, "target_player");
        } catch (IllegalArgumentException e) {
            return context.getSource().getPlayer();
        }
    }

}
