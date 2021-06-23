package com.fibermc.essentialcommands;

import com.fibermc.essentialcommands.util.StringBuilderPlus;
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
import java.util.Map;
import java.util.Objects;

public class Config {

    private static SortedProperties props;
    private static final String CONFIG_PATH = "./config/EssentialCommands.properties";

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
    public static boolean OPS_BYPASS_TELEPORT_RULES;

    private static final String KEY_FORMATTING_DEFAULT = "formatting_default";
    private static final String KEY_FORMATTING_ACCENT = "formatting_accent";
    private static final String KEY_FORMATTING_ERROR = "formatting_error";
    private static final String KEY_ENABLE_BACK = "enable_back";
    private static final String KEY_ENABLE_HOME = "enable_home";
    private static final String KEY_ENABLE_SPAWN = "enable_spawn";
    private static final String KEY_ENABLE_TPA = "enable_tpa";
    private static final String KEY_ENABLE_WARP = "enable_warp";
    private static final String KEY_HOME_LIMIT = "home_limit";
    private static final String KEY_TELEPORT_COOLDOWN = "teleport_cooldown";
    private static final String KEY_TELEPORT_DELAY = "teleport_delay";
    private static final String KEY_ALLOW_BACK_ON_DEATH = "allow_back_on_death";
    private static final String KEY_TELEPORT_REQUEST_DURATION = "teleport_request_duration";
    private static final String KEY_USE_PERMISSIONS_API = "use_permissions_api";
    private static final String KEY_CHECK_FOR_UPDATES = "check_for_updates";
    private static final String KEY_TELEPORT_INTERRUPT_ON_DAMAGED = "teleport_interrupt_on_damaged";
    private static final String KEY_ALLOW_TELEPORT_BETWEEN_DIMENSIONS = "allow_teleport_between_dimensions";
    private static final String KEY_OPS_BYPASS_TELEPORT_RULES = "ops_bypass_teleport_rules";

    public static void loadOrCreateProperties() {
        props = new SortedProperties();
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

    private static void initProperties() {
        Map<String, Object> defProps = Map.ofEntries(
            new SimpleEntry<>(KEY_FORMATTING_DEFAULT,                 "gold"),
            new SimpleEntry<>(KEY_FORMATTING_ACCENT,                  "light_purple"),
            new SimpleEntry<>(KEY_FORMATTING_ERROR,                   "red"),
            new SimpleEntry<>(KEY_ENABLE_BACK,                        String.valueOf(true)),
            new SimpleEntry<>(KEY_ENABLE_HOME,                        String.valueOf(true)),
            new SimpleEntry<>(KEY_ENABLE_SPAWN,                       String.valueOf(true)),
            new SimpleEntry<>(KEY_ENABLE_TPA,                         String.valueOf(true)),
            new SimpleEntry<>(KEY_ENABLE_WARP,                        String.valueOf(true)),
            new SimpleEntry<>(KEY_HOME_LIMIT,                         String.valueOf(-1)),
            new SimpleEntry<>(KEY_TELEPORT_COOLDOWN,                  String.valueOf(1D)),
            new SimpleEntry<>(KEY_TELEPORT_DELAY,                     String.valueOf(0D)),
            new SimpleEntry<>(KEY_ALLOW_BACK_ON_DEATH,                String.valueOf(false)),
            new SimpleEntry<>(KEY_TELEPORT_REQUEST_DURATION,          String.valueOf(60)),
            new SimpleEntry<>(KEY_USE_PERMISSIONS_API,                String.valueOf(false)),
            new SimpleEntry<>(KEY_CHECK_FOR_UPDATES,                  String.valueOf(true)),
            new SimpleEntry<>(KEY_TELEPORT_INTERRUPT_ON_DAMAGED,      String.valueOf(true)),
            new SimpleEntry<>(KEY_ALLOW_TELEPORT_BETWEEN_DIMENSIONS,  String.valueOf(true)),
            new SimpleEntry<>(KEY_OPS_BYPASS_TELEPORT_RULES,          String.valueOf(true))
        );

        // If property did not exist in file, load it into props now from defaults.
        defProps.forEach((key, value) -> props.putIfAbsent(key, value));

        styleJsonDeserializer = new Style.Serializer();
        jsonParser = new JsonParser();

        FORMATTING_DEFAULT  = parseStyleOrDefault(                  (String) props.get(KEY_FORMATTING_DEFAULT),  (String)defProps.get(KEY_FORMATTING_DEFAULT));
        FORMATTING_ACCENT   = parseStyleOrDefault(                  (String) props.get(KEY_FORMATTING_ACCENT),   (String)defProps.get(KEY_FORMATTING_ACCENT));
        FORMATTING_ERROR    = parseStyleOrDefault(                  (String) props.get(KEY_FORMATTING_ERROR),    (String)defProps.get(KEY_FORMATTING_ERROR));
        ENABLE_BACK         = Boolean.parseBoolean(                 (String) props.get(KEY_ENABLE_BACK));
        ENABLE_HOME         = Boolean.parseBoolean(                 (String) props.get(KEY_ENABLE_HOME));
        ENABLE_SPAWN        = Boolean.parseBoolean(                 (String) props.get(KEY_ENABLE_SPAWN));
        ENABLE_TPA          = Boolean.parseBoolean(                 (String) props.get(KEY_ENABLE_TPA));
        ENABLE_WARP         = Boolean.parseBoolean(                 (String) props.get(KEY_ENABLE_WARP));
        HOME_LIMIT          = Integer.parseInt(                     (String) props.get(KEY_HOME_LIMIT));
        TELEPORT_COOLDOWN   = Double.parseDouble(                   (String) props.get(KEY_TELEPORT_COOLDOWN));
        TELEPORT_DELAY      = Double.parseDouble(                   (String) props.get(KEY_TELEPORT_DELAY));
        ALLOW_BACK_ON_DEATH = Boolean.parseBoolean(                 (String) props.get(KEY_ALLOW_BACK_ON_DEATH));
        TELEPORT_REQUEST_DURATION = Integer.parseInt(               (String) props.get(KEY_TELEPORT_REQUEST_DURATION));
        USE_PERMISSIONS_API = Boolean.parseBoolean(                 (String) props.get(KEY_USE_PERMISSIONS_API));
        CHECK_FOR_UPDATES = Boolean.parseBoolean(                   (String) props.get(KEY_CHECK_FOR_UPDATES));
        TELEPORT_INTERRUPT_ON_DAMAGED = Boolean.parseBoolean(       (String) props.get(KEY_TELEPORT_INTERRUPT_ON_DAMAGED));
        ALLOW_TELEPORT_BETWEEN_DIMENSIONS = Boolean.parseBoolean(   (String) props.get(KEY_ALLOW_TELEPORT_BETWEEN_DIMENSIONS));
        OPS_BYPASS_TELEPORT_RULES = Boolean.parseBoolean(           (String) props.get(KEY_OPS_BYPASS_TELEPORT_RULES));

        try {
            Objects.requireNonNull(FORMATTING_DEFAULT);
            Objects.requireNonNull(FORMATTING_ACCENT);
            Objects.requireNonNull(FORMATTING_ERROR);
        } catch (NullPointerException e) {
            EssentialCommands.log(Level.ERROR, "Something went wrong while loading chat styles from EssentialCommands.properties. Additionally, default values could not be loaded.");
            e.printStackTrace();
        }

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

            props.storeSorted(writer, new StringBuilderPlus()
                .appendLine("Essential Commands Properties")
                .append("Config Documentation: https://github.com/John-Paul-R/Essential-Commands/wiki/Config-Documentation")
                .toString()
            );
        } catch (IOException e) {
            EssentialCommands.log(Level.WARN,"Failed to store preferences to disk.");
        }

    }

}
