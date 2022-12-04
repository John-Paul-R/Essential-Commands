package com.fibermc.essentialcommands.util;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.*;

import com.fibermc.essentialcommands.EssentialCommands;
import com.fibermc.essentialcommands.ManagerLocator;
import com.fibermc.essentialcommands.WorldDataManager;
import com.fibermc.essentialcommands.types.MinecraftLocation;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;

import net.minecraft.world.World;

import static com.fibermc.essentialcommands.EssentialCommands.LOGGER;

public class EssentialsConvertor {
    private static final Yaml YAML_INSTANCE = new Yaml();
    private static final Map<String, String> COMPARISON_TABLE = new HashMap<>(){{
        put("world", "minecraft:overworld");
        put("world_nether", "minecraft:the_nether");
        put("world_the_end", "minecraft:the_end");
    }};

    public static final Path OLD_CONF_PATH = Path.of("./config", "EssentialCommands", "Old-Config");

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


    public static void warpConvert() {
        File oldWarpsDictionary = OLD_CONF_PATH.toFile();
        if (!oldWarpsDictionary.exists() || oldWarpsDictionary.isFile()) {
            oldWarpsDictionary.mkdirs();
        }

        List<File> oldWarpFiles = getAllFile(oldWarpsDictionary);
        if(oldWarpFiles.size() > 0){
            LOGGER.info("Found the old warp file(s), converting!");

            WorldDataManager worldDataManager = ManagerLocator.getInstance().getWorldDataManager();
            Map<String, World> worldMap = new HashMap<>();
            Map<String, MinecraftLocation> locationMap = new HashMap<>();
            int counter = 0;

            for(World world : EssentialCommands.WORLD_LIST){
                worldMap.put(world.getRegistryKey().getValue().toString(), world);
            }

            for(File oldWarpFile : oldWarpFiles){
                try{
                    if((oldWarpFile.getName().contains("yaml") || oldWarpFile.getName().contains("yml")) && !oldWarpFile.getName().contains(".converted")){
                        Map<String, Object> data = YAML_INSTANCE.load(new FileInputStream(oldWarpFile));
                        double x = (double) data.get("x");
                        double y = (double) data.get("y");
                        double z = (double) data.get("z");
                        float yaw = (float) ((double) data.get("yaw"));
                        float pitch = (float) ((double) data.get("pitch"));
                        String worldName = (String) data.get("world-name");
                        String warpName = (String) data.get("name");
                        locationMap.put(warpName, new MinecraftLocation(worldMap.get(COMPARISON_TABLE.get(worldName)).getRegistryKey(), x, y, z, yaw, pitch));
                        oldWarpFile.renameTo(new File(oldWarpsDictionary, oldWarpFile.getName() + ".converted"));
                    }
                }
                catch (Exception e){
                    LOGGER.error("There was an error occurred while converting warp config:" + oldWarpFile.getName());
                    e.printStackTrace();
                    return;
                }
            }

            for(Map.Entry<String, MinecraftLocation> entry : locationMap.entrySet()){
                try{
                    counter++;
                    worldDataManager.setWarp(entry.getKey(), entry.getValue(), false);
                }
                catch (Exception e){
                    LOGGER.error("There was an error occurred while putting warp config:" + entry.getKey());
                    e.printStackTrace();
                }
            }

            LOGGER.info("Convert finished, converted " + counter + " file(s)!");
        }
    }
}
