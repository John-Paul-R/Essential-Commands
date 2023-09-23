package com.fibermc.essentialcommands.util;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.*;

import com.fibermc.essentialcommands.ManagerLocator;
import com.fibermc.essentialcommands.WorldDataManager;
import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.playerdata.PlayerData;
import com.fibermc.essentialcommands.types.MinecraftLocation;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

import static com.fibermc.essentialcommands.EssentialCommands.LOGGER;

@SuppressWarnings("unchecked")
public final class EssentialsConvertor {
    private EssentialsConvertor() {}

    private static final Yaml YAML_INSTANCE = new Yaml();
    private static final Map<String, String> COMPARISON_TABLE = new HashMap<>() {{
        put("world", "minecraft:overworld");
        put("world_nether", "minecraft:the_nether");
        put("world_the_end", "minecraft:the_end");
    }};

    public static final Path OLD_USERDATA_PATH = Path.of("./config", "EssentialCommands", "Old-Config", "UsersData");

    @NotNull
    private static List<File> getAllFile(File dirFile) {
        List<File> files = new ArrayList<>();

        if (Objects.isNull(dirFile) || !dirFile.exists() || dirFile.isFile()) return files;

        File[] childrenFiles = dirFile.listFiles();
        if (Objects.isNull(childrenFiles) || childrenFiles.length == 0) return files;

        for (File childFile : childrenFiles) {
            if (childFile.isFile()) {
                files.add(childFile);
            }
        }
        return files;
    }

    // TODO @jp: currently unused because EssentialsXParser.convertPlayerDataDir exists, but there
    //  are some good ideas here that might be worth carrying over
    public static void homeConvert(MinecraftServer server) {
        File oldUsersDataDictionary = OLD_USERDATA_PATH.toFile();
        if (!oldUsersDataDictionary.exists() || oldUsersDataDictionary.isFile()) {
            oldUsersDataDictionary.mkdirs();
        }

        List<File> oldUserDataFiles = getAllFile(oldUsersDataDictionary);
        if (oldUserDataFiles.size() > 0) {
            LOGGER.info("Found the old home file(s), converting!");

            Map<String, ServerWorld> worldMap = new HashMap<>();
            int counter = 0;

            for (ServerWorld world : server.getWorlds()) {
                worldMap.put(world.getRegistryKey().getValue().toString(), world);
            }

            for (File oldUserDataFile : oldUserDataFiles) {
                try {
                    if ((oldUserDataFile.getName().contains("yaml") || oldUserDataFile.getName().contains("yml"))
                        && !oldUserDataFile.getName().contains(".converted")) {
                        Map<String, Object> data = YAML_INSTANCE.load(new FileInputStream(oldUserDataFile));

                        if (data.containsKey("homes")) {
                            String playerName = (String) data.get("last-account-name");
                            Optional<GameProfile> playerCache = server.getUserCache().findByName(playerName);

                            GameProfile playerProfile = new GameProfile(UUID.fromString(oldUserDataFile.getName().replaceAll(".yml", "").replaceAll(".yaml", "")), playerName);

                            Map<String, Map<String, Object>> homesMap = (Map<String, Map<String, Object>>) data.get("homes");
                            for (Map.Entry<String, Map<String, Object>> entry : homesMap.entrySet()) {
                                ServerWorld world = worldMap.get(COMPARISON_TABLE.get((String) entry.getValue().get("world-name")));
                                ServerPlayerEntity player = new ServerPlayerEntity(server, world, playerProfile, SyncedClientOptions.createDefault());

                                PlayerData playerData = ((ServerPlayerEntityAccess) player).ec$getPlayerData();

                                double x = (double) entry.getValue().get("x");
                                double y = (double) entry.getValue().get("y");
                                double z = (double) entry.getValue().get("z");
                                float yaw = (float) ((double) entry.getValue().get("yaw"));
                                float pitch = (float) ((double) entry.getValue().get("pitch"));
                                String homeName = entry.getKey();

                                playerData.addHome(homeName, new MinecraftLocation(world.getRegistryKey(), x, y, z, yaw, pitch));
                                playerData.save();

                                oldUserDataFile.renameTo(new File(oldUsersDataDictionary, oldUserDataFile.getName() + ".converted"));
                                counter++;
                            }
                        } else {
                            oldUserDataFile.renameTo(new File(oldUsersDataDictionary, oldUserDataFile.getName() + ".converted"));
                        }
                    }
                } catch (CommandSyntaxException ex) {
                    LOGGER.error("An unexpected error occurred while converting home config: {}", oldUserDataFile.getName());
                    ex.printStackTrace();
                } catch (Exception ex) {
                    LOGGER.error("An unexpected error occurred while converting home config: {}", oldUserDataFile.getName());
                    ex.printStackTrace();
                    return;
                }
            }

            LOGGER.info("Convert finished, converted " + counter + " file(s)!");
        }
    }

    public static void warpConvert(MinecraftServer server, File essentialsXWarpsDirectory) {

        List<File> oldWarpFiles = getAllFile(essentialsXWarpsDirectory);

        if (oldWarpFiles.size() == 0) {
            LOGGER.info("Found 0 warps to convert in '{}'. Exiting.", essentialsXWarpsDirectory.getPath());
            return;
        }

        LOGGER.info("Found {} old warp file(s), converting!", oldWarpFiles.size());

        WorldDataManager worldDataManager = ManagerLocator.getInstance().getWorldDataManager();
        Map<String, World> worldMap = new HashMap<>();
        Map<String, MinecraftLocation> locationMap = new HashMap<>();

        for (World world : server.getWorlds()) {
            worldMap.put(world.getRegistryKey().getValue().toString(), world);
        }

        for (File oldWarpFile : oldWarpFiles) {
            try {
                if (oldWarpFile.getName().endsWith("yaml") || oldWarpFile.getName().endsWith("yml")) {
                    Map<String, Object> data = YAML_INSTANCE.load(new FileInputStream(oldWarpFile));
                    double x = (double) data.get("x");
                    double y = (double) data.get("y");
                    double z = (double) data.get("z");
                    float yaw = (float) ((double) data.get("yaw"));
                    float pitch = (float) ((double) data.get("pitch"));
                    String worldName = (String) data.get("world-name");
                    String warpName = (String) data.get("name");
                    locationMap.put(
                        warpName,
                        new MinecraftLocation(
                            worldMap.get(COMPARISON_TABLE.get(worldName)).getRegistryKey(),
                            x, y, z, yaw, pitch));
                }
            } catch (Exception e) {
                LOGGER.error("An unexpected error occurred while converting warp config: {}", oldWarpFile.getName());
                e.printStackTrace();
            }
        }

        int successfulConversionCount = 0;
        for (Map.Entry<String, MinecraftLocation> entry : locationMap.entrySet()) {
            try {
                worldDataManager.setWarp(entry.getKey(), entry.getValue(), false);
                successfulConversionCount++;
            } catch (Exception e) {
                LOGGER.error("There was an error occurred while putting warp config: {}", entry.getKey());
                e.printStackTrace();
            }
        }

        LOGGER.info(
            "Convert finished, successfully converted {} / {} file(s)!",
            successfulConversionCount,
            oldWarpFiles.size());
    }
}
