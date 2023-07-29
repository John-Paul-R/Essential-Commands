package com.fibermc.essentialcommands.util;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.fibermc.essentialcommands.EssentialCommands;
import io.netty.util.CharsetUtil;
import org.apache.logging.log4j.Level;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;

public final class FileUtil {
    private FileUtil() {}

    public static Path getOrCreateWorldDirectory(MinecraftServer server, String subdir) throws IOException {
        Path dataDirectoryPath;
        try {
            dataDirectoryPath = Files.createDirectories(server.getSavePath(WorldSavePath.ROOT).resolve(subdir));
        } catch (NullPointerException e) {
            dataDirectoryPath = Files.createDirectories(Paths.get("./world/%s".formatted(subdir)));
            EssentialCommands.log(Level.WARN, "Session save path could not be found. Defaulting to ./world/{}", subdir);
        }

        return dataDirectoryPath;
    }

    static Charset[] charsetsToTry = new Charset[] {CharsetUtil.UTF_8, CharsetUtil.UTF_16};

    public static String readString(Path filePath) throws IOException {
        for (var charset : charsetsToTry) {
            try {
                return Files.readString(filePath, charset);
            } catch (Exception ex) {
                // ign
            }
        }

        throw new IOException("Failed to read string from file: %s".formatted(filePath));
    }
}
