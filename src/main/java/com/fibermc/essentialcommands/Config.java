package com.fibermc.essentialcommands;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonParser;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.Objects;

public class Config {

    public static SortedProperties props = new SortedProperties();

    private static String CONFIG_PATH = "./config/EssentialCommands.properties";

    public static Style FORMATTING_DEFAULT;
    public static Style FORMATTING_ACCENT;
    public static Style FORMATTING_ERROR;
    public static boolean ENABLE_BACK;
    public static boolean ENABLE_HOME;
    public static boolean ENABLE_SPAWN;
    public static boolean ENABLE_TPA;
    public static boolean ENABLE_WARP;
    public static int HOME_LIMIT;
    public static double TELEPORT_COOLDOWN;
    public static double TELEPORT_DELAY;
    public static boolean ALLOW_BACK_ON_DEATH;
    public static int TELEPORT_REQUEST_DURATION;
    public static boolean USE_PERMISSIONS_API;
    public static boolean CHECK_FOR_UPDATES;
    public static boolean TELEPORT_INTERRUPT_ON_DAMAGED;
    public static boolean ALLOW_TELEPORT_BETWEEN_DIMENSIONS;

    public static void loadOrCreateProperties() {
        File inFile = new File(CONFIG_PATH);

        try{
            boolean fileAlreadyExisted = !inFile.createNewFile();
            if (fileAlreadyExisted) {
                props.load(new FileReader(inFile));
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
            new SimpleEntry<>("enable_back", "true"),
            new SimpleEntry<>("enable_home", "true"),
            new SimpleEntry<>("enable_spawn", "true"),
            new SimpleEntry<>("enable_tpa", "true"),
            new SimpleEntry<>("enable_warp", "true"),
            new SimpleEntry<>("home_limit", "-1"),
            new SimpleEntry<>("teleport_cooldown", "1D"),
            new SimpleEntry<>("teleport_delay", "0D"),
            new SimpleEntry<>("allow_back_on_death", "false"),
            new SimpleEntry<>("teleport_request_duration", "60"),
            new SimpleEntry<>("use_permissions_api", "false"),
            new SimpleEntry<>("check_for_updates", "true"),
            new SimpleEntry<>("teleport_interrupt_on_damaged", "true"),
            new SimpleEntry<>("allow_teleport_between_dimensions", "true")
        );
        for (SimpleEntry<String, String> property : defProps) {
            props.putIfAbsent(property.getKey(), property.getValue());
        }
        styleJsonDeserializer = new Style.Serializer();
        jsonParser = new JsonParser();

        FORMATTING_DEFAULT = parseStyleOrDefault(
            (String)props.get(defProps.get(0).getKey()),
            defProps.get(0).getValue()
        );
        FORMATTING_ACCENT = parseStyleOrDefault(
            (String)props.get(defProps.get(1).getKey()),
            defProps.get(1).getValue()
        );
        FORMATTING_ERROR = parseStyleOrDefault(
            (String)props.get(defProps.get(2).getKey()),
            defProps.get(2).getValue()
        );
        ENABLE_BACK         = Boolean.parseBoolean((String)     props.get(defProps.get(3).getKey()));
        ENABLE_HOME         = Boolean.parseBoolean((String)     props.get(defProps.get(4).getKey()));
        ENABLE_SPAWN        = Boolean.parseBoolean((String)     props.get(defProps.get(5).getKey()));
        ENABLE_TPA          = Boolean.parseBoolean((String)     props.get(defProps.get(6).getKey()));
        ENABLE_WARP         = Boolean.parseBoolean((String)     props.get(defProps.get(7).getKey()));
        HOME_LIMIT          = Integer.parseInt((String)         props.get(defProps.get(8).getKey()));
        TELEPORT_COOLDOWN   = Double.parseDouble((String)       props.get(defProps.get(9).getKey()));
        TELEPORT_DELAY      = Double.parseDouble((String)       props.get(defProps.get(10).getKey()));
        ALLOW_BACK_ON_DEATH = Boolean.parseBoolean((String)     props.get(defProps.get(11).getKey()));
        TELEPORT_REQUEST_DURATION = Integer.parseInt((String)   props.get(defProps.get(12).getKey()));
        USE_PERMISSIONS_API = Boolean.parseBoolean((String)     props.get(defProps.get(13).getKey()));
        CHECK_FOR_UPDATES = Boolean.parseBoolean((String)       props.get(defProps.get(14).getKey()));
        TELEPORT_INTERRUPT_ON_DAMAGED = Boolean.parseBoolean((String) props.get(defProps.get(15).getKey()));
        ALLOW_TELEPORT_BETWEEN_DIMENSIONS = Boolean.parseBoolean((String) props.get(defProps.get(16).getKey()));

        Objects.requireNonNull(FORMATTING_DEFAULT);
        Objects.requireNonNull(FORMATTING_ACCENT);
        Objects.requireNonNull(FORMATTING_ERROR);
//        FORMATTING_DEFAULT = FORMATTING_DEFAULT == null ? Formatting.byName(defProps.get(0).getValue()) : FORMATTING_DEFAULT;
//        FORMATTING_ACCENT = FORMATTING_ACCENT == null ? Formatting.byName(defProps.get(1).getValue()) : FORMATTING_ACCENT;
//        FORMATTING_ERROR = FORMATTING_ERROR == null ? Formatting.byName(defProps.get(2).getValue()) : FORMATTING_ERROR;
    }

    private static JsonDeserializer<Style> styleJsonDeserializer;
    private static JsonParser jsonParser;
    private static Style parseStyleOrDefault(String styleStr, String defaultStyleStr) {
        Style outStyle = parseStyle(styleStr);
        if (Objects.isNull(outStyle)) {
            outStyle = parseStyle(defaultStyleStr);
            EssentialCommands.log(
                Level.WARN,
                String.format("Could not load malformed style: '%s'. Using default, '%s'.", styleStr, defaultStyleStr)
            );
        }
        return outStyle;
    }

    private static Style parseStyle(String styleStr) {
        Style outStyle = null;
        Formatting formatting = Formatting.byName(styleStr);
        if (Objects.nonNull(formatting)) {
            outStyle = Style.EMPTY.withFormatting(formatting);
        }
        
        if (Objects.isNull(outStyle)) {
            outStyle = styleJsonDeserializer.deserialize(
                jsonParser.parse(styleStr),
                null, null
            );
        }

        return outStyle;
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
