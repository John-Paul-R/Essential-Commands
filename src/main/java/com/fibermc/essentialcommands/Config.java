package com.fibermc.essentialcommands;

import com.fibermc.essentialcommands.util.StringBuilderPlus;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
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
    public static boolean ENABLE_NICK;
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
    public static boolean NICKNAMES_IN_PLAYER_LIST;
    public static Text NICKNAME_PREFIX;
    public static int NICKNAME_MAX_LENGTH;


    private static final String KEY_FORMATTING_DEFAULT = "formatting_default";
    private static final String KEY_FORMATTING_ACCENT = "formatting_accent";
    private static final String KEY_FORMATTING_ERROR = "formatting_error";
    private static final String KEY_ENABLE_BACK = "enable_back";
    private static final String KEY_ENABLE_HOME = "enable_home";
    private static final String KEY_ENABLE_SPAWN = "enable_spawn";
    private static final String KEY_ENABLE_TPA = "enable_tpa";
    private static final String KEY_ENABLE_WARP = "enable_warp";
    private static final String KEY_ENABLE_NICK = "enable_nick";
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
    private static final String KEY_NICKNAMES_IN_PLAYER_LIST = "nicknames_in_player_list";
    private static final String KEY_NICKNAME_PREFIX = "nickname_prefix";
    private static final String KEY_NICKNAME_MAX_LENGTH = "nickname_max_length";

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
        styleJsonDeserializer = new Style.Serializer();
        jsonParser = new JsonParser();

        FORMATTING_DEFAULT  = parseStyleOrDefault(                  (String) props.get(KEY_FORMATTING_DEFAULT), "gold");
        FORMATTING_ACCENT   = parseStyleOrDefault(                  (String) props.get(KEY_FORMATTING_ACCENT),  "light_purple");
        FORMATTING_ERROR    = parseStyleOrDefault(                  (String) props.get(KEY_FORMATTING_ERROR),   "red");
        ENABLE_BACK         = Boolean.parseBoolean(                 (String) props.getOrDefault(KEY_ENABLE_BACK, String.valueOf(true)));
        ENABLE_HOME         = Boolean.parseBoolean(                 (String) props.getOrDefault(KEY_ENABLE_HOME, String.valueOf(true)));
        ENABLE_SPAWN        = Boolean.parseBoolean(                 (String) props.getOrDefault(KEY_ENABLE_SPAWN, String.valueOf(true)));
        ENABLE_TPA          = Boolean.parseBoolean(                 (String) props.getOrDefault(KEY_ENABLE_TPA, String.valueOf(true)));
        ENABLE_WARP         = Boolean.parseBoolean(                 (String) props.getOrDefault(KEY_ENABLE_WARP, String.valueOf(true)));
        ENABLE_NICK         = Boolean.parseBoolean(                 (String) props.getOrDefault(KEY_ENABLE_NICK, String.valueOf(true)));
        HOME_LIMIT          = parseInt(                             (String) props.getOrDefault(KEY_HOME_LIMIT, String.valueOf(-1)));
        TELEPORT_COOLDOWN   = parseDouble(                          (String) props.getOrDefault(KEY_TELEPORT_COOLDOWN, String.valueOf(1D)));
        TELEPORT_DELAY      = parseDouble(                          (String) props.getOrDefault(KEY_TELEPORT_DELAY, String.valueOf(0D)));
        ALLOW_BACK_ON_DEATH = Boolean.parseBoolean(                 (String) props.getOrDefault(KEY_ALLOW_BACK_ON_DEATH, String.valueOf(false)));
        TELEPORT_REQUEST_DURATION = parseInt(                       (String) props.getOrDefault(KEY_TELEPORT_REQUEST_DURATION, String.valueOf(60)));
        USE_PERMISSIONS_API = Boolean.parseBoolean(                 (String) props.getOrDefault(KEY_USE_PERMISSIONS_API, String.valueOf(false)));
        CHECK_FOR_UPDATES = Boolean.parseBoolean(                   (String) props.getOrDefault(KEY_CHECK_FOR_UPDATES, String.valueOf(true)));
        TELEPORT_INTERRUPT_ON_DAMAGED = Boolean.parseBoolean(       (String) props.getOrDefault(KEY_TELEPORT_INTERRUPT_ON_DAMAGED, String.valueOf(true)));
        ALLOW_TELEPORT_BETWEEN_DIMENSIONS = Boolean.parseBoolean(   (String) props.getOrDefault(KEY_ALLOW_TELEPORT_BETWEEN_DIMENSIONS, String.valueOf(true)));
        OPS_BYPASS_TELEPORT_RULES = Boolean.parseBoolean(           (String) props.getOrDefault(KEY_OPS_BYPASS_TELEPORT_RULES, String.valueOf(true)));
        NICKNAMES_IN_PLAYER_LIST  = Boolean.parseBoolean(           (String) props.getOrDefault(KEY_NICKNAMES_IN_PLAYER_LIST, String.valueOf(true)));
        NICKNAME_PREFIX     = parseTextOrDefault(                   (String) props.get(KEY_NICKNAME_PREFIX), "{\"text\":\"~\",\"color\":\"red\"}");
        NICKNAME_MAX_LENGTH = parseInt(                             (String) props.getOrDefault(KEY_NICKNAME_MAX_LENGTH, String.valueOf(32)));

        try {
            Objects.requireNonNull(FORMATTING_DEFAULT);
            Objects.requireNonNull(FORMATTING_ACCENT);
            Objects.requireNonNull(FORMATTING_ERROR);
        } catch (NullPointerException e) {
            EssentialCommands.log(Level.ERROR, "Something went wrong while loading chat styles from EssentialCommands.properties. Additionally, default values could not be loaded.");
            e.printStackTrace();
        }

        props.putIfAbsent(KEY_FORMATTING_DEFAULT,                   String.valueOf(styleJsonDeserializer.serialize(FORMATTING_DEFAULT, null, null)));
        props.putIfAbsent(KEY_FORMATTING_ACCENT,                    String.valueOf(styleJsonDeserializer.serialize(FORMATTING_ACCENT, null, null)));
        props.putIfAbsent(KEY_FORMATTING_ERROR,                     String.valueOf(styleJsonDeserializer.serialize(FORMATTING_ERROR, null, null)));
        props.putIfAbsent(KEY_ENABLE_BACK,                          String.valueOf(ENABLE_BACK));
        props.putIfAbsent(KEY_ENABLE_HOME,                          String.valueOf(ENABLE_HOME));
        props.putIfAbsent(KEY_ENABLE_SPAWN,                         String.valueOf(ENABLE_SPAWN));
        props.putIfAbsent(KEY_ENABLE_TPA,                           String.valueOf(ENABLE_TPA));
        props.putIfAbsent(KEY_ENABLE_WARP,                          String.valueOf(ENABLE_WARP));
        props.putIfAbsent(KEY_ENABLE_NICK,                          String.valueOf(ENABLE_NICK));
        props.putIfAbsent(KEY_HOME_LIMIT,                           String.valueOf(HOME_LIMIT));
        props.putIfAbsent(KEY_TELEPORT_COOLDOWN,                    String.valueOf(TELEPORT_COOLDOWN));
        props.putIfAbsent(KEY_TELEPORT_DELAY,                       String.valueOf(TELEPORT_DELAY));
        props.putIfAbsent(KEY_ALLOW_BACK_ON_DEATH,                  String.valueOf(ALLOW_BACK_ON_DEATH));
        props.putIfAbsent(KEY_TELEPORT_REQUEST_DURATION,            String.valueOf(TELEPORT_REQUEST_DURATION));
        props.putIfAbsent(KEY_USE_PERMISSIONS_API,                  String.valueOf(USE_PERMISSIONS_API));
        props.putIfAbsent(KEY_CHECK_FOR_UPDATES,                    String.valueOf(CHECK_FOR_UPDATES));
        props.putIfAbsent(KEY_TELEPORT_INTERRUPT_ON_DAMAGED,        String.valueOf(TELEPORT_INTERRUPT_ON_DAMAGED));
        props.putIfAbsent(KEY_ALLOW_TELEPORT_BETWEEN_DIMENSIONS,    String.valueOf(ALLOW_TELEPORT_BETWEEN_DIMENSIONS));
        props.putIfAbsent(KEY_OPS_BYPASS_TELEPORT_RULES,            String.valueOf(OPS_BYPASS_TELEPORT_RULES));
        props.putIfAbsent(KEY_NICKNAMES_IN_PLAYER_LIST,             String.valueOf(NICKNAMES_IN_PLAYER_LIST));
        props.putIfAbsent(KEY_NICKNAME_PREFIX,                      Text.Serializer.toJson(NICKNAME_PREFIX));
        props.putIfAbsent(KEY_NICKNAME_MAX_LENGTH,                  String.valueOf(NICKNAME_MAX_LENGTH));

    }

    private static Style.Serializer styleJsonDeserializer;
    private static JsonParser jsonParser;
    private static Style parseStyleOrDefault(String styleStr, String defaultStyleStr) {
        Style outStyle = null;
        if (Objects.nonNull(styleStr)) {
            outStyle = parseStyle(styleStr);
        }

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
            try {
                outStyle = styleJsonDeserializer.deserialize(
                    jsonParser.parse(styleStr),
                    null, null
                );
            } catch (JsonSyntaxException e) {
                EssentialCommands.log(Level.ERROR, String.format(
                    "Malformed Style JSON in config: %s", styleStr
                ));
//                e.printStackTrace();
            }

        }

        return outStyle;
    }

    private static Text parseTextOrDefault(String textStr, String defaultTextStr) {
        Text outText = null;
        if (textStr != null) {
            outText = parseText(textStr);
        }

        if (outText == null) {
            outText = parseText(defaultTextStr);
            EssentialCommands.log(
                Level.WARN,
                String.format("Could not load malformed Text: '%s'. Using default, '%s'.", textStr, defaultTextStr)
            );
        }
        return outText;
    }

    private static Text parseText(String textStr) {
        return Text.Serializer.fromJson(textStr);
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

    private static int parseInt(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            logNumberParseError(s, "int");
        }
        return -1;
    }
    private static double parseDouble(String s) {
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            logNumberParseError(s, "double");
        }
        return -1;
    }
    private static void logNumberParseError(String num, String type) {
        EssentialCommands.log(Level.WARN, String.format(
            "Invalid number format for type '%s' in config. Value provided: '%s'", type, num
        ));
    }
}
