package com.fibermc.essentialcommands.commands;

import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.parsers.NodeParser;
import eu.pb4.placeholders.api.parsers.ParserBuilder;
import eu.pb4.placeholders.api.parsers.TagParser;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;

public final class MotdCommand {
    private MotdCommand() {}

    private static final NodeParser NODE_PARSER = ParserBuilder.of()
        .globalPlaceholders()
        .add(TagParser.QUICK_TEXT_WITH_STF)
        .build();

    public static int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var player = context.getSource().getPlayerOrException();
        exec(player);
        return 0;
    }

    public static void exec(ServerPlayer player) {
        player.createCommandSourceStack().sendSuccess(
            () -> Placeholders.parseText(
                NODE_PARSER.parseNode(CONFIG.MOTD),
                PlaceholderContext.of(player)
            ),
            false
        );
    }
}
