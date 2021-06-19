package com.fibermc.essentialcommands;

import net.minecraft.util.Formatting;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;

import com.fibermc.essentialcommands.SortedProperties;

public class Config {

    public static SortedProperties props = new SortedProperties();

    private static String CONFIG_PATH = "./config/EssentialCommands.properties";

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
    public static boolean USE_PERMISSIONS_API;

    public static void loadOrCreateProperties() {
        File inFile = new File(CONFIG_PATH);

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
        List<SimpleEntry<String, String>> defProps = List.of(
            new SimpleEntry<>("formatting_default", "gold"),
            new SimpleEntry<>("formatting_accent", "light_purple"),
            new SimpleEntry<>("formatting_error", "red"),
            new SimpleEntry<>("enable_home", "true"),
            new SimpleEntry<>("enable_tpa", "true"),
            new SimpleEntry<>("enable_back", "true"),
            new SimpleEntry<>("enable_warp", "true"),
            new SimpleEntry<>("home_limit", "-1"),
            new SimpleEntry<>("teleport_cooldown", "1D"),
            new SimpleEntry<>("teleport_delay", "0D"),
            new SimpleEntry<>("allow_back_on_death", "false"),
            new SimpleEntry<>("teleport_request_duration", "60"),
            new SimpleEntry<>("use_permissions_api", "false")
        );
        for (SimpleEntry<String, String> property : defProps) {
            props.putIfAbsent(property.getKey(), property.getValue());
        }

        FORMATTING_DEFAULT  = Formatting.byName((String)        props.get(defProps.get(0).getKey()));
        FORMATTING_ACCENT   = Formatting.byName((String)        props.get(defProps.get(1).getKey()));
        FORMATTING_ERROR    = Formatting.byName((String)        props.get(defProps.get(2).getKey()));
        ENABLE_HOME         = Boolean.parseBoolean((String)     props.get(defProps.get(3).getKey()));
        ENABLE_TPA          = Boolean.parseBoolean((String)     props.get(defProps.get(4).getKey()));
        ENABLE_BACK         = Boolean.parseBoolean((String)     props.get(defProps.get(5).getKey()));
        ENABLE_WARP         = Boolean.parseBoolean((String)     props.get(defProps.get(6).getKey()));
        HOME_LIMIT          = Integer.parseInt((String)         props.get(defProps.get(7).getKey()));
        TELEPORT_COOLDOWN   = Double.parseDouble((String)       props.get(defProps.get(8).getKey()));
        TELEPORT_DELAY      = Double.parseDouble((String)       props.get(defProps.get(9).getKey()));
        ALLOW_BACK_ON_DEATH = Boolean.parseBoolean((String)     props.get(defProps.get(10).getKey()));
        TELEPORT_REQUEST_DURATION = Integer.parseInt((String)   props.get(defProps.get(11).getKey()));
        USE_PERMISSIONS_API = Boolean.parseBoolean((String)     props.get(defProps.get(12).getKey()));
    }

    public static void storeProperties() {
        try{
            File outFile = new File(CONFIG_PATH);
            FileWriter writer = new FileWriter(outFile);

            props.storeSorted(writer, "Essential Commands Properties");
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
