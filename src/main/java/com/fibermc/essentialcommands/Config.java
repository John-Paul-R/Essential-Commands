package com.fibermc.essentialcommands;

import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

public class Config {

    public static Properties prefs = new Properties();
    static {
    }

    public static String FORMATTING_DEFAULT;
    public static String FORMATTING_ACCENT;
    public static String FORMATTING_ERROR;
    public static boolean ENABLE_HOME;
    public static boolean ENABLE_TPA;
    public static boolean ENABLE_BACK;
    public static int HOME_LIMIT;
    public static double TELEPORT_COOLDOWN;
    public static double TELEPORT_DELAY;

    public static void loadOrCreateProperties() {
        File inFile = new File("./config/EssentialCommands.json");

        try{
            boolean fileAlreadyExisted = !inFile.createNewFile();
            if (fileAlreadyExisted) {
                FileReader reader = new FileReader(inFile);

                prefs.load(reader);
            } else {

            }
        } catch (IOException e) {
            LogManager.getLogger(Config.class).warn("[EssentialCommands] Failed to load or save preferences.");
        }
        initProperties();
        storeProperties();
    }

    public static void initProperties() {
        prefs.putIfAbsent("FORMATTING_DEFAULT", "gold");
        prefs.putIfAbsent("formatting_accent", "blue");
        prefs.putIfAbsent("formatting_error", "red");
        prefs.putIfAbsent("enable_home", "true");
        prefs.putIfAbsent("enable_tpa", "true");
        prefs.putIfAbsent("enable_back", "true");
        prefs.putIfAbsent("home_limit", "-1");
        prefs.putIfAbsent("teleport_cooldown", "1D");
        prefs.putIfAbsent("teleport_delay", "0D");

        FORMATTING_DEFAULT  = (String)  prefs.getOrDefault("FORMATTING_DEFAULT", "gold");
        FORMATTING_ACCENT   = (String)  prefs.getOrDefault("formatting_accent", "blue");
        FORMATTING_ERROR    = (String)  prefs.getOrDefault("formatting_error", "red");
        ENABLE_HOME         = Boolean.parseBoolean((String) prefs.getOrDefault("enable_home", "true"));
        ENABLE_TPA          = Boolean.parseBoolean((String) prefs.getOrDefault("enable_tpa", "true"));
        ENABLE_BACK         = Boolean.parseBoolean((String) prefs.getOrDefault("enable_back", "true"));
        HOME_LIMIT          = Integer.parseInt((String) prefs.getOrDefault("home_limit", "-1"));
        TELEPORT_COOLDOWN   = Double.parseDouble((String)  prefs.getOrDefault("teleport_cooldown", "1D"));
        TELEPORT_DELAY      = Double.parseDouble((String)  prefs.getOrDefault("teleport_delay", "0D"));
    }

    public static void storeProperties() {
        try{
            File outFile = new File("./config/EssentialCommands.json");
            FileWriter writer = new FileWriter(outFile);

            prefs.store(writer, "");
        } catch (IOException e) {
            //todo catch
        }

    }

//    static {
//        prefs.put("FORMATTING_DEFAULT", "gold");
//        prefs.put("formatting_accent", "blue");
//        prefs.put("formatting_accent", "red");
//
//        prefs.putBoolean("enable_home", true);
//        prefs.putBoolean("enable_tpa", true);
//        prefs.putBoolean("enable_back", true);
//
//        prefs.putInt("home_limit", -1);
//        prefs.putDouble("teleport_cooldown", 1D);
//        prefs.putDouble("teleport_delay", 0D);
//    }

}
