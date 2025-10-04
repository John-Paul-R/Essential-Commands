package com.fibermc.essentialcommands.commands;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import com.fibermc.essentialcommands.EssentialCommands;
import com.fibermc.essentialcommands.playerdata.PlayerData;
import com.fibermc.essentialcommands.util.FileUtil;
import net.codebox.homoglyph.Homoglyph;
import net.codebox.homoglyph.HomoglyphBuilder;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

public final class DisallowedWordsCommand {

    private DisallowedWordsCommand() {}

    private static Text rulesText;

    public static int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        context.getSource().sendFeedback(() -> rulesText, false);
        return 0;
    }

    public static int reloadCommand(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var playerData = PlayerData.accessFromContextOrThrow(context);

        try {
            reload(context.getSource().getServer());
            playerData.sendCommandFeedback("disallowedwords.reload.success");
        } catch (Exception e) {
            playerData.sendCommandError("disallowedwords.reload.error.unexpected");
            e.printStackTrace();
        }
        return 0;
    }

    public static final class DisallowWordSystem {
        private final Homoglyph homoglyph;
        private final ArrayList<String> disallowedWords;
        private static Path disallowedWordsFile;
        private static final Comparator<String> ORDER = String.CASE_INSENSITIVE_ORDER;

        private static MinecraftServer currentServer;
        private static DisallowWordSystem instance;

        public static DisallowWordSystem current(MinecraftServer server) {
            if (currentServer != server) {
                instance = create(server);
            }
            return instance;
        }

        static DisallowWordSystem create(MinecraftServer server) {
            try {
                currentServer = server;
                disallowedWordsFile = FileUtil.FilePaths.current(server).disallowedWordsFile();
                if (FileUtil.createFileWithDirs(disallowedWordsFile)) {
                    EssentialCommands.LOGGER.info("Created disallowed words file at path: {}", disallowedWordsFile);
                }
                return instance = new DisallowWordSystem(
                    Files.readAllLines(disallowedWordsFile, FileUtil.detectCharset(disallowedWordsFile))
                );
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

        public DisallowWordSystem(Collection<String> disallowedWords) throws IOException {
            homoglyph = HomoglyphBuilder.build();
            this.disallowedWords = new ArrayList<>(disallowedWords);
            this.disallowedWords.sort(ORDER);
        }

        private void save() {
            Util.getIoWorkerExecutor().execute(() -> {
                try {
                    Files.write(disallowedWordsFile, disallowedWords, StandardCharsets.UTF_8);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }

        public boolean isAllowed(String text) {
            var result = homoglyph.search(text, disallowedWords);
            return result.isEmpty();
        }

        public boolean disallow(String text) {
            // the index of the search key, if it is contained in the list;
            // otherwise, (-(insertion point) - 1). The insertion point is
            // defined as the point at which the key would be inserted into the
            // list: the index of the first element greater than the key, or
            // list.size() if all elements in the list are less than the
            // specified key. Note that this guarantees that the return value
            // will be >= 0 if and only if the key is found.
            int index = Collections.binarySearch(disallowedWords, text, ORDER);
            if (index >= 0) {
                // already exists
                return false;
            }

            disallowedWords.add(-(index + 1), text);

            save();

            return true;
        }

        public boolean allow(String text) {
            int index = Collections.binarySearch(disallowedWords, text, ORDER);
            if (index < 0) {
                // is not disallowed
                return false;
            }

            disallowedWords.remove(index);

            save();

            return true;
        }
    }

    public static final class Disallow implements Command<ServerCommandSource> {
        public static final String WORD_ARG = "word";

        @Override
        public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
            var playerData = PlayerData.accessFromContextOrThrow(context);

            var word = StringArgumentType.getString(context, WORD_ARG);

            var wordBlocker = DisallowWordSystem.current(context.getSource().getServer());

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

            var wordBlocker = DisallowWordSystem.current(context.getSource().getServer());

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

            var wordBlocker = DisallowWordSystem.current(context.getSource().getServer());

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

            var wordBlocker = DisallowWordSystem.current(context.getSource().getServer());

            if (wordBlocker.disallowedWords.isEmpty()) {
                playerData.sendCommandFeedback("disallowedwords.list.empty");
            } else {
                context.getSource().sendFeedback(
                    () -> Text.literal(
                        "Disallowed words (" + wordBlocker.disallowedWords.size() + "): "
                            + String.join(", ", wordBlocker.disallowedWords)
                    ),
                    false
                );
            }

            return SINGLE_SUCCESS;
        }
    }

    public static void reload(MinecraftServer server) {
        DisallowedWordsCommand.DisallowWordSystem.create(server);
    }
}
