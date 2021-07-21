package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.util.TextUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.CommandNode;
import net.minecraft.server.command.ServerCommandSource;

public final class CommandUtil {

    private CommandUtil() {}

    public static String getCommandString(ServerCommandSource source, CommandNode<ServerCommandSource> commandNode) {
        CommandDispatcher<ServerCommandSource> dispatcher = source.getMinecraftServer().getCommandManager().getDispatcher();

        return "/" + TextUtil.joinStrings(
                dispatcher.getPath(commandNode),
                CommandDispatcher.ARGUMENT_SEPARATOR
        );
    }

}
