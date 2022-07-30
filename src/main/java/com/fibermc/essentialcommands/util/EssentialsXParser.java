package com.fibermc.essentialcommands.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.fibermc.essentialcommands.EssentialCommands;
import com.fibermc.essentialcommands.mixin.PersistentStateManagerInvoker;
import com.fibermc.essentialcommands.playerdata.PlayerData;
import com.fibermc.essentialcommands.playerdata.PlayerDataFactory;
import com.fibermc.essentialcommands.types.MinecraftLocation;
import com.fibermc.essentialcommands.types.NamedLocationStorage;
import org.apache.logging.log4j.Level;
import org.yaml.snakeyaml.Yaml;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

public final class EssentialsXParser {
    private EssentialsXParser() {}

    public static NamedLocationStorage parsePlayerHomes(File yamlSource, MinecraftServer server, Map<UUID, RegistryKey<World>> uuidRegistryKeyMap) {
        NamedLocationStorage homes = new NamedLocationStorage();
        String yamlStr = null;
        try {
            yamlStr = Files.readString(yamlSource.toPath());
        } catch (IOException e) {
            e.printStackTrace();
        }
//        server.getWorlds().forEach(world -> world.);
        Yaml yaml = new Yaml();
        Map<Object, Object> ydoc = yaml.load(yamlStr);
        Map<String, Map<String, Object>> homesMap = (Map<String, Map<String, Object>>) ydoc.get("homes");
        homesMap.forEach((String name, Map<String, Object> locData) -> {
            RegistryKey<World> worldRegistryKey = uuidRegistryKeyMap.get(UUID.fromString((String) locData.get("world")));
            if (worldRegistryKey == null) {
                worldRegistryKey = World.OVERWORLD;
            }

            homes.put(
                name,
                new MinecraftLocation(
                    worldRegistryKey,
                    (Double) locData.get("x"),
                    (Double) locData.get("y"),
                    (Double) locData.get("z"),
                    ((Double) locData.get("yaw")).floatValue(),
                    ((Double) locData.get("pitch")).floatValue()
                ));
        });

        return homes;
    }

    public record WorldUids(UUID overworld, UUID nether, UUID end) {}

//    public static void WorldUids

    public static Map<UUID, RegistryKey<World>> getWorldUids(MinecraftServer server) {

        Map<UUID, RegistryKey<World>> uuidRegistryKeyMap = new LinkedHashMap<>();
        server.getWorlds().forEach(world -> {
            File uidFile = ((PersistentStateManagerInvoker) world.getPersistentStateManager()).invokeGetFile("uid");

            try {
                EssentialCommands.LOGGER.info(String.format("Attempting to read file: %s", uidFile.getPath()));
                byte[] uuidBytes = Files.readAllBytes(uidFile.toPath());
                EssentialCommands.LOGGER.info(String.format("File: %s, UUID: %s", uidFile.getPath(), UUID.nameUUIDFromBytes(uuidBytes)));
                uuidRegistryKeyMap.put(
                    UUID.nameUUIDFromBytes(uuidBytes),
                    world.getRegistryKey()
                );
            } catch (IOException e) {
                EssentialCommands.LOGGER.log(
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

        File[] playerDataFiles = Objects.requireNonNull(sourceDir.listFiles());

        for (File file : playerDataFiles) {
            NamedLocationStorage homes = parsePlayerHomes(file, server, worldUuidRegistryKeyMap);
            // WARN: Currently, this will still overwrite player's new homes with EssentialsX homes.
            String targetFilePathStr = targetPath.resolve(file.getName()).toString();
            targetFilePathStr = targetFilePathStr.substring(0, targetFilePathStr.indexOf(".yml")) + ".dat";
            File targetFile = new File(targetFilePathStr);

            PlayerData playerData = PlayerDataFactory.create(homes, targetFile);
            playerData.save();
        }

    }

}
