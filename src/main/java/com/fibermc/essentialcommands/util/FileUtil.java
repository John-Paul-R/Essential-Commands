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
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.WorldSavePath;

public final class FileUtil {
    private FileUtil() {}

    private static Path getOrCreateWorldDirectory(MinecraftServer server, String subdir) throws IOException {
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

    /**
     * @return whether the file was created
     */
    public static boolean createFileWithDirs(Path path) throws IOException {
        var file = path.toFile();
        file.getParentFile().mkdirs();
        return file.createNewFile();
    }

    public static final class FilePaths {
        public static final Path CONFIG = Path.of("./config/EssentialCommands.properties");

        private static MinecraftServer currentServer;
        private static Inst inst;

        public static Inst current(MinecraftServer server) {
            if (currentServer != server) {
                create(server);
            }
            return inst;
        }

        public record Inst(
            Path ecWorldDataDir,
            Path ecPlayerDataDir,
            Path ecPlayerProfilesDir,
            Path rulesFile,
            Path disallowedWordsFile
        ) {
            public Path playerDataFilePath(ServerPlayerEntity player) {
                return this.ecPlayerDataDir()
                    .resolve(player.getUuidAsString() + ".dat");
            }

            public Path playerProfileFile(ServerPlayerEntity player) {
                return this.ecPlayerProfilesDir()
                    .resolve(player.getUuidAsString() + ".dat");
            }
        }

        public static Inst create(MinecraftServer minecraftServer) {
            currentServer = minecraftServer;
            Path mcDir = minecraftServer.getRunDirectory();
            Path configDir = mcDir.resolve("config");
            Path ecConfigDir = configDir.resolve("essentialcommands");
            try {
                return inst = new Inst(
                    getOrCreateWorldDirectory(minecraftServer, "essentialcommands"),
                    getOrCreateWorldDirectory(minecraftServer, "modplayerdata"),
                    getOrCreateWorldDirectory(minecraftServer, "ec_player_profiles"),
                    ecConfigDir.resolve("rules.txt"),
                    ecConfigDir.resolve("disallowed-words.txt")
                );
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
