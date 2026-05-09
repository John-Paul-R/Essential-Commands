package com.fibermc.essentialcommands.commands;

import eu.pb4.placeholders.api.ServerPlaceholderContext;
import eu.pb4.placeholders.api.parsers.NodeParser;
import eu.pb4.placeholders.api.parsers.ParserBuilder;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;

public final class MotdCommand {
    private MotdCommand() {}

    private static final NodeParser NODE_PARSER = ParserBuilder.of()
        .commonPlaceholders()
        .serverPlaceholders()
        .quickText()
        .simplifiedTextFormat()
        .build();

    public static int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var player = context.getSource().getPlayerOrException();
        exec(player);
        return 0;
    }

    public static void exec(ServerPlayer player) {
        player.createCommandSourceStack().sendSuccess(
            () -> NODE_PARSER.parseComponent(
                NODE_PARSER.parseNode(CONFIG.MOTD),
                ServerPlaceholderContext.of(player).asParserContext()
            ),
            false
        );
    }
}
