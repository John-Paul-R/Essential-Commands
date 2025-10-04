package com.fibermc.essentialcommands;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import com.fibermc.essentialcommands.util.FileUtil;
import net.codebox.homoglyph.Homoglyph;
import net.codebox.homoglyph.HomoglyphBuilder;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;

public final class DisallowWordsSystem {
    private final Homoglyph homoglyph;
    private final ArrayList<String> disallowedWords;
    private static Path disallowedWordsFile;
    private static final Comparator<String> ORDER = String.CASE_INSENSITIVE_ORDER;

    private static MinecraftServer currentServer;
    private static DisallowWordsSystem instance;

    public static DisallowWordsSystem current(MinecraftServer server) {
        if (currentServer != server) {
            instance = create(server);
        }
        return instance;
    }

    public static DisallowWordsSystem create(MinecraftServer server) {
        try {
            currentServer = server;
            disallowedWordsFile = FileUtil.FilePaths.current(server).disallowedWordsFile();
            if (FileUtil.createFileWithDirs(disallowedWordsFile)) {
                EssentialCommands.LOGGER.info("Created disallowed words file at path: {}", disallowedWordsFile);
            }
            return instance = new DisallowWordsSystem(
                Files.readAllLines(disallowedWordsFile, FileUtil.detectCharset(disallowedWordsFile))
            );
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public DisallowWordsSystem(Collection<String> disallowedWords) throws IOException {
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

    public Collection<String> words() {
        return this.disallowedWords;
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
