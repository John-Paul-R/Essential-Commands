package com.fibermc.essentialcommands.commands;

import eu.pb4.placeholders.api.PlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.TextParserUtils;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;

public final class MotdCommand {
    private MotdCommand() {}

    public static int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var player = context.getSource().getPlayerOrThrow();
        exec(player);
        return 0;
    }

    public static void exec(ServerPlayerEntity player) {
        var message = Placeholders.parseText(
            TextParserUtils.formatText(CONFIG.MOTD),
            PlaceholderContext.of(player)
        );
        player.getCommandSource().sendFeedback(() -> message, false);
    }
}
