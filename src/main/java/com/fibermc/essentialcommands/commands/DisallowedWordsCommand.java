package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.DisallowWordsSystem;
import com.fibermc.essentialcommands.EssentialCommands;
import com.fibermc.essentialcommands.playerdata.PlayerData;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public final class DisallowedWordsCommand {

    private DisallowedWordsCommand() {}

    public static int reloadCommand(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var playerData = PlayerData.accessFromContextOrThrow(context);

        try {
            reload(context.getSource().getServer());
            playerData.sendCommandFeedback("disallowedwords.reload.success");
        } catch (Exception e) {
            playerData.sendCommandError("disallowedwords.reload.error.unexpected");
            EssentialCommands.LOGGER.error("Error reloading disallowed files", e);
        }
        return 0;
    }

    public static void reload(MinecraftServer server) {
        DisallowWordsSystem.create(server);
    }

    public static final class Disallow implements Command<ServerCommandSource> {
        public static final String WORD_ARG = "word";

        @Override
        public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
            var playerData = PlayerData.accessFromContextOrThrow(context);

            var word = StringArgumentType.getString(context, WORD_ARG);

            var wordBlocker = DisallowWordsSystem.current(context.getSource().getServer());

            var justDisallowed = wordBlocker.disallow(word);

            if (justDisallowed) {
                playerData.sendCommandFeedback("disallowedwords.disallow.success", Text.literal(word));
            } else {
                playerData.sendCommandFeedback("disallowedwords.disallow.error.already_disallowed", Text.literal(word));
            }

            return SINGLE_SUCCESS;
        }
    }

    public static final class Allow implements Command<ServerCommandSource> {
        public static final String WORD_ARG = "word";

        @Override
        public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
            var playerData = PlayerData.accessFromContextOrThrow(context);

            var word = StringArgumentType.getString(context, WORD_ARG);

            var wordBlocker = DisallowWordsSystem.current(context.getSource().getServer());

            var justAllowed = wordBlocker.allow(word);

            if (justAllowed) {
                playerData.sendCommandFeedback("disallowedwords.allow.success", Text.literal(word));
            } else {
                playerData.sendCommandFeedback("disallowedwords.allow.error.not_disallowed", Text.literal(word));
            }

            return SINGLE_SUCCESS;
        }
    }

    public static final class Test implements Command<ServerCommandSource> {
        public static final String TEXT_ARG = "text";

        @Override
        public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
            var playerData = PlayerData.accessFromContextOrThrow(context);

            var text = StringArgumentType.getString(context, TEXT_ARG);

            var wordBlocker = DisallowWordsSystem.current(context.getSource().getServer());

            var isAllowed = wordBlocker.isAllowed(text);

            if (isAllowed) {
                playerData.sendCommandFeedback("disallowedwords.test.allowed");
            } else {
                playerData.sendCommandFeedback("disallowedwords.test.disallowed");
            }

            return SINGLE_SUCCESS;
        }
    }

    public static final class List implements Command<ServerCommandSource> {
        @Override
        public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
            var playerData = PlayerData.accessFromContextOrThrow(context);

            var wordBlocker = DisallowWordsSystem.current(context.getSource().getServer());
            var disallowedWords = wordBlocker.words();

            if (disallowedWords.isEmpty()) {
                playerData.sendCommandFeedback("disallowedwords.list.empty");
            } else {
                context.getSource().sendFeedback(
                    () -> Text.literal(
                        "Disallowed words (" + disallowedWords.size() + "): "
                            + String.join(", ", disallowedWords)
                    ),
                    false
                );
            }

            return SINGLE_SUCCESS;
        }
    }
}
