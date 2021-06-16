package com.fibermc.essentialcommands;

import net.minecraft.util.Formatting;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class Config {

    public static Properties props = new Properties();
    static {
    }

    public static Formatting FORMATTING_DEFAULT;
    public static Formatting FORMATTING_ACCENT;
    public static Formatting FORMATTING_ERROR;
    public static boolean ENABLE_HOME;
    public static boolean ENABLE_TPA;
    public static boolean ENABLE_BACK;
    public static boolean ENABLE_WARP;
    public static int HOME_LIMIT;
    public static double TELEPORT_COOLDOWN;
    public static double TELEPORT_DELAY;
    public static boolean ALLOW_BACK_ON_DEATH;
    public static int TELEPORT_REQUEST_DURATION;

    public static void loadOrCreateProperties() {
        File inFile = new File("./config/EssentialCommands.json");

        try{
            boolean fileAlreadyExisted = !inFile.createNewFile();
            if (fileAlreadyExisted) {
                FileReader reader = new FileReader(inFile);

                props.load(reader);
            } else {

            }
        } catch (IOException e) {
            EssentialCommands.log(Level.WARN,"Failed to load preferences.");
        }
        initProperties();
        storeProperties();
    }

    public static void initProperties() {
        props.putIfAbsent("formatting_default", "gold");
        props.putIfAbsent("formatting_accent", "light_purple");
        props.putIfAbsent("formatting_error", "red");
        props.putIfAbsent("enable_home", "true");
        props.putIfAbsent("enable_tpa", "true");
        props.putIfAbsent("enable_back", "true");
        props.putIfAbsent("enable_warp", "true");
        props.putIfAbsent("home_limit", "-1");
        props.putIfAbsent("teleport_cooldown", "1D");
        props.putIfAbsent("teleport_delay", "0D");
        props.putIfAbsent("allow_back_on_death", "false");
        props.putIfAbsent("teleport_request_duration", "60");


        FORMATTING_DEFAULT  = Formatting.byName((String)  props.getOrDefault("formatting_default", "gold"));
        FORMATTING_ACCENT   = Formatting.byName((String)  props.getOrDefault("formatting_accent", "light_purple"));
        FORMATTING_ERROR    = Formatting.byName((String)  props.getOrDefault("formatting_error", "red"));
        ENABLE_HOME         = Boolean.parseBoolean((String) props.getOrDefault("enable_home", "true"));
        ENABLE_TPA          = Boolean.parseBoolean((String) props.getOrDefault("enable_tpa", "true"));
        ENABLE_BACK         = Boolean.parseBoolean((String) props.getOrDefault("enable_back", "true"));
        ENABLE_WARP         = Boolean.parseBoolean((String) props.getOrDefault("enable_warp", "true"));
        HOME_LIMIT          = Integer.parseInt((String) props.getOrDefault("home_limit", "-1"));
        TELEPORT_COOLDOWN   = Double.parseDouble((String)  props.getOrDefault("teleport_cooldown", "1D"));
        TELEPORT_DELAY      = Double.parseDouble((String)  props.getOrDefault("teleport_delay", "0D"));
        ALLOW_BACK_ON_DEATH = Boolean.parseBoolean((String) props.getOrDefault("allow_back_on_death", "false"));
        TELEPORT_REQUEST_DURATION = Integer.parseInt((String) props.getOrDefault("teleport_request_duration", "60"));
    }

    public static void storeProperties() {
        try{
            File outFile = new File("./config/EssentialCommands.properties");
            FileWriter writer = new FileWriter(outFile);

            props.store(writer, "");
        } catch (IOException e) {
            EssentialCommands.log(Level.WARN,"Failed to store preferences to disk.");
        }

    }

//    static {
//        props.put("FORMATTING_DEFAULT", "gold");
//        props.put("formatting_accent", "blue");
//        props.put("formatting_accent", "red");
//
//        props.putBoolean("enable_home", true);
//        props.putBoolean("enable_tpa", true);
//        props.putBoolean("enable_back", true);
//
//        props.putInt("home_limit", -1);
//        props.putDouble("teleport_cooldown", 1D);
//        props.putDouble("teleport_delay", 0D);
//    }

}
