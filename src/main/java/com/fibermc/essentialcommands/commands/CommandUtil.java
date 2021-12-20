package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.util.TextUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.server.command.ServerCommandSource;

public final class CommandUtil {

    private CommandUtil() {}

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



}
