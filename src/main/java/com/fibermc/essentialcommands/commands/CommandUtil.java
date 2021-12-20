package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.util.TextUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public final class CommandUtil {

    private CommandUtil() {}

    public static RequiredArgumentBuilder<ServerCommandSource, EntitySelector> targetPlayerArgument() {
        return CommandManager.argument("target_player", EntityArgumentType.player());
    }

    public static String getCommandString(ServerCommandSource source, CommandNode<ServerCommandSource> commandNode) {
        CommandDispatcher<ServerCommandSource> dispatcher = source.getServer().getCommandManager().getDispatcher();

        return "/" + TextUtil.joinStrings(
                dispatcher.getPath(commandNode),
                CommandDispatcher.ARGUMENT_SEPARATOR
        );
    }

    public static CommandSyntaxException createSimpleException(Message msg) {
        return new CommandSyntaxException(new SimpleCommandExceptionType(msg), msg);
    }

    public static ServerPlayerEntity getCommandTargetPlayer(CommandContext<ServerCommandSource> context) throws CommandSyntaxException{
        try {
            return EntityArgumentType.getPlayer(context, "target_player");
        } catch (IllegalArgumentException e) {
            return context.getSource().getPlayer();
        }
    }

}
