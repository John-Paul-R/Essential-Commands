package com.fibermc.essentialcommands.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.util.*;

import com.fibermc.essentialcommands.mixin.PersistentStateManagerInvoker;
import com.fibermc.essentialcommands.playerdata.PlayerData;
import com.fibermc.essentialcommands.playerdata.PlayerDataFactory;
import com.fibermc.essentialcommands.types.NamedLocationStorage;
import com.fibermc.essentialcommands.types.NamedMinecraftLocation;
import org.apache.logging.log4j.Level;
import org.yaml.snakeyaml.Yaml;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

import static com.fibermc.essentialcommands.EssentialCommands.LOGGER;

public final class EssentialsXParser {
    private EssentialsXParser() {}

    public static NamedLocationStorage parsePlayerHomes(
        File yamlSource,
        Map<UUID, RegistryKey<World>> uuidRegistryKeyMap
    ) {
        NamedLocationStorage homes = new NamedLocationStorage();
        String yamlStr = null;
        try {
            yamlStr = Files.readString(yamlSource.toPath());
        } catch (IOException e) {
            LOGGER.error("Error while reading yaml file {}", yamlSource);
            LOGGER.error("Err Detail:", e);
        }

        Yaml yaml = new Yaml();
        Map<Object, Object> ydoc = yaml.load(yamlStr);
        Map<String, Map<String, Object>> homesMap = (Map<String, Map<String, Object>>) ydoc.get("homes");
        LOGGER.info("Found {} homes in file '{}'.", homesMap.size(), yamlSource.toPath().toString());
        homesMap.forEach((String name, Map<String, Object> locData) -> {
            RegistryKey<World> worldRegistryKey = uuidRegistryKeyMap.get(UUID.fromString((String) locData.get("world")));
            if (worldRegistryKey == null) {
                worldRegistryKey = World.OVERWORLD;
            }

            homes.put(
                name,
                new NamedMinecraftLocation(
                    worldRegistryKey,
                    (Double) locData.get("x"),
                    (Double) locData.get("y"),
                    (Double) locData.get("z"),
                    ((Double) locData.get("yaw")).floatValue(),
                    ((Double) locData.get("pitch")).floatValue(),
                    name
                ));
        });

        return homes;
    }

    public static Map<UUID, RegistryKey<World>> getWorldUids(MinecraftServer server) {

        Map<UUID, RegistryKey<World>> uuidRegistryKeyMap = new LinkedHashMap<>();
        server.getWorlds().forEach(world -> {
            // This is dumb. We're taking fabric/vanilla's ideas of these worlds to look for the
            // bukkit/spigot/paper UID. Instead, we should be reading those mods' config files to
            // find the correct directory. Rn this will essentially (heh) always be wrong.
            var persistentStateManager = ((PersistentStateManagerInvoker) world.getPersistentStateManager());
            File uidFile = persistentStateManager.invokeGetFile("uid");
            if (!uidFile.exists()) {
                uidFile = persistentStateManager.invokeGetFile("../uid");
            }

            try {
                LOGGER.info(String.format("Attempting to read file: %s", uidFile.getPath()));
                byte[] uuidBytes = Files.readAllBytes(uidFile.toPath());
                LOGGER.info(String.format("File: %s, UUID: %s", uidFile.getPath(), UUID.nameUUIDFromBytes(uuidBytes)));
                uuidRegistryKeyMap.put(
                    UUID.nameUUIDFromBytes(uuidBytes),
                    world.getRegistryKey()
                );
            } catch (IOException e) {
                LOGGER.log(
                    Level.WARN,
                    String.format(
                        "World, %s, did not have a valid uid.dat file. EssentialsX homes set in this dim will likely be remapped to %s.",
                        world.getRegistryKey().getValue().toString(),
                        World.OVERWORLD.getValue().toString()
                    )
                );
            }
        });

        return uuidRegistryKeyMap;
    }

    public static void convertPlayerDataDir(File sourceDir, File targetDir, MinecraftServer server) throws NotDirectoryException, FileNotFoundException {
        if (!sourceDir.exists()) {
            throw new FileNotFoundException(sourceDir.getAbsolutePath() + " does not exist!");
        }
        if (!sourceDir.isDirectory()) {
            throw new NotDirectoryException(sourceDir.getAbsolutePath() + " is not a directory!");
        }

        var worldUuidRegistryKeyMap = getWorldUids(server);

        targetDir.mkdir();
        // Existing EC player data files
        Path targetPath = targetDir.toPath();

        var filesArr = Objects.requireNonNull(sourceDir.listFiles());

        LOGGER.info("Preparing to convert homes for {} players in directory '{}'", filesArr, sourceDir);

        int filesAttempted = 0;
        int filesSucceeded = 0;
        var playerDataFiles = Arrays.stream(filesArr)
            .filter(file -> file.getName().endsWith("yml"))
            .toList();

        for (File file : playerDataFiles) {
            filesAttempted++;
            LOGGER.info("Begin pasring homes for '{}'", file);

            NamedLocationStorage homes = parsePlayerHomes(file, worldUuidRegistryKeyMap);
            // WARN: Currently, this will still overwrite player's new homes of the same name with
            // EssentialsX homes.
            String targetFilePathStr = targetPath.resolve(file.getName()).toString();
            targetFilePathStr = targetFilePathStr.substring(0, targetFilePathStr.indexOf(".yml")) + ".dat";
            File targetFile = new File(targetFilePathStr);

            LOGGER.info("Creating temporary playerdata for '{}', with {} homes.", file, homes.size());
            try {
                PlayerData playerData = PlayerDataFactory.create(homes, targetFile);
                playerData.save();
                filesSucceeded++;
            } catch (Exception ex) {
                LOGGER.error("An unexpected error occurred while parsing player data file '{}'", targetFile.getPath(), ex);
            }
        }

        LOGGER.info(
            "Finished converting EssentialsX homes to EssentialCommands homes. (Players Attempted: {}, Players Succeeded: {})",
            filesAttempted,
            filesSucceeded);
    }

}
