package com.fibermc.essentialcommands.config;

import com.fibermc.essentialcommands.EssentialCommands;
import com.fibermc.essentialcommands.util.StringBuilderPlus;
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
import java.util.Objects;

import static com.fibermc.essentialcommands.util.TextUtil.parseText;

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
    public static boolean ENABLE_RTP;
    public static boolean ENABLE_FLY;
    public static boolean ENABLE_WORKBENCH;
    public static boolean ENABLE_ENDERCHEST;
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
    public static int RTP_RADIUS;
    public static int RTP_COOLDOWN;
    public static int RTP_MAX_ATTEMPTS;
    public static boolean BROADCAST_TO_OPS;
    public static boolean NICK_REVEAL_ON_HOVER;

    private static final Option<Boolean> _ENABLE_BACK =         new Option<>("enable_back", true, Boolean::parseBoolean);
    private static final Option<Boolean> _ENABLE_HOME =         new Option<>("enable_home", true, Boolean::parseBoolean);
    private static final Option<Boolean> _ENABLE_SPAWN =        new Option<>("enable_spawn", true, Boolean::parseBoolean);
    private static final Option<Boolean> _ENABLE_TPA =          new Option<>("enable_tpa", true, Boolean::parseBoolean);
    private static final Option<Boolean> _ENABLE_WARP =         new Option<>("enable_warp", true, Boolean::parseBoolean);
    private static final Option<Boolean> _ENABLE_NICK =         new Option<>("enable_nick", true, Boolean::parseBoolean);
    private static final Option<Boolean> _ENABLE_RTP =          new Option<>("enable_rtp", true, Boolean::parseBoolean);
    private static final Option<Boolean> _ENABLE_FLY =          new Option<>("enable_fly", true, Boolean::parseBoolean);
    private static final Option<Boolean> _ENABLE_WORKBENCH =    new Option<>("enable_workbench", true, Boolean::parseBoolean);
    private static final Option<Boolean> _ENABLE_ENDERCHEST =   new Option<>("enable_enderchest", true, Boolean::parseBoolean);
    private static final Option<Integer> _HOME_LIMIT =                  new Option<>("home_limit", 1, Config::parseInt);
    private static final Option<Double>  _TELEPORT_COOLDOWN =           new Option<>("teleport_cooldown", 1.0, Config::parseDouble);
    private static final Option<Double>  _TELEPORT_DELAY =              new Option<>("teleport_delay", 0.0, Config::parseDouble);
    private static final Option<Boolean> _ALLOW_BACK_ON_DEATH =         new Option<>("allow_back_on_death", false, Boolean::parseBoolean);
    private static final Option<Integer> _TELEPORT_REQUEST_DURATION =   new Option<>("teleport_request_duration", 60, Config::parseInt);
    private static final Option<Boolean> _USE_PERMISSIONS_API =         new Option<>("use_permissions_api", false, Boolean::parseBoolean);
    private static final Option<Boolean> _CHECK_FOR_UPDATES =           new Option<>("check_for_updates", true, Boolean::parseBoolean);
    private static final Option<Boolean> _TELEPORT_INTERRUPT_ON_DAMAGED = new Option<>("teleport_interrupt_on_damaged", true, Boolean::parseBoolean);
    private static final Option<Boolean> _ALLOW_TELEPORT_BETWEEN_DIMENSIONS = new Option<>("allow_teleport_between_dimensions", true, Boolean::parseBoolean);
    private static final Option<Boolean> _OPS_BYPASS_TELEPORT_RULES =   new Option<>("ops_bypass_teleport_rules", true, Boolean::parseBoolean);
    private static final Option<Boolean> _NICKNAMES_IN_PLAYER_LIST =    new Option<>("nicknames_in_player_list", true, Boolean::parseBoolean);
    private static final Option<Integer> _NICKNAME_MAX_LENGTH = new Option<>("nickname_max_length", 32, Config::parseInt);
    private static final Option<Integer> _RTP_RADIUS = new Option<>("rtp_radius", 1000, Config::parseInt);
    private static final Option<Integer> _RTP_COOLDOWN = new Option<>("rtp_cooldown", 30, Config::parseInt);
    private static final Option<Integer> _RTP_MAX_ATTEMPTS = new Option<>("rtp_max_attempts", 15, Config::parseInt);
    private static final Option<Boolean> _BROADCAST_TO_OPS = new Option<>("broadcast_to_ops", false, Boolean::parseBoolean);
    private static final Option<Boolean> _NICK_REVEAL_ON_HOVER = new Option<>("nick_reveal_on_hover", true, Boolean::parseBoolean);

    private static final String KEY_FORMATTING_DEFAULT = "formatting_default";
    private static final String KEY_FORMATTING_ACCENT = "formatting_accent";
    private static final String KEY_FORMATTING_ERROR = "formatting_error";
    private static final String KEY_NICKNAME_PREFIX = "nickname_prefix";

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

        FORMATTING_DEFAULT  = parseStyleOrDefault((String) props.get(KEY_FORMATTING_DEFAULT), "gold");
        FORMATTING_ACCENT   = parseStyleOrDefault((String) props.get(KEY_FORMATTING_ACCENT),  "light_purple");
        FORMATTING_ERROR    = parseStyleOrDefault((String) props.get(KEY_FORMATTING_ERROR),   "red");
        NICKNAME_PREFIX     = parseTextOrDefault((String) props.get(KEY_NICKNAME_PREFIX), "{\"text\":\"~\",\"color:\"red\"}");

        ENABLE_BACK         = _ENABLE_BACK.loadAndSave(props).getValue();
        ENABLE_HOME         = _ENABLE_HOME.loadAndSave(props).getValue();
        ENABLE_SPAWN        = _ENABLE_SPAWN.loadAndSave(props).getValue();
        ENABLE_TPA          = _ENABLE_TPA.loadAndSave(props).getValue();
        ENABLE_WARP         = _ENABLE_WARP.loadAndSave(props).getValue();
        ENABLE_NICK         = _ENABLE_NICK.loadAndSave(props).getValue();
        ENABLE_RTP          = _ENABLE_RTP.loadAndSave(props).getValue();
        ENABLE_FLY          = _ENABLE_FLY.loadAndSave(props).getValue();
        ENABLE_WORKBENCH    = _ENABLE_WORKBENCH.loadAndSave(props).getValue();
        ENABLE_ENDERCHEST    = _ENABLE_ENDERCHEST.loadAndSave(props).getValue();
        HOME_LIMIT          = _HOME_LIMIT.loadAndSave(props).getValue();
        TELEPORT_COOLDOWN   = _TELEPORT_COOLDOWN.loadAndSave(props).getValue();
        TELEPORT_DELAY      = _TELEPORT_DELAY.loadAndSave(props).getValue();
        ALLOW_BACK_ON_DEATH = _ALLOW_BACK_ON_DEATH.loadAndSave(props).getValue();
        TELEPORT_REQUEST_DURATION = _TELEPORT_REQUEST_DURATION.loadAndSave(props).getValue();
        USE_PERMISSIONS_API = _USE_PERMISSIONS_API.loadAndSave(props).getValue();
        CHECK_FOR_UPDATES = _CHECK_FOR_UPDATES.loadAndSave(props).getValue();
        TELEPORT_INTERRUPT_ON_DAMAGED = _TELEPORT_INTERRUPT_ON_DAMAGED.loadAndSave(props).getValue();
        ALLOW_TELEPORT_BETWEEN_DIMENSIONS = _ALLOW_TELEPORT_BETWEEN_DIMENSIONS.loadAndSave(props).getValue();
        OPS_BYPASS_TELEPORT_RULES = _OPS_BYPASS_TELEPORT_RULES.loadAndSave(props).getValue();
        NICKNAMES_IN_PLAYER_LIST  = _NICKNAMES_IN_PLAYER_LIST.loadAndSave(props).getValue();
        NICKNAME_MAX_LENGTH = _NICKNAME_MAX_LENGTH.loadAndSave(props).getValue();
        RTP_RADIUS =          _RTP_RADIUS.loadAndSave(props).getValue();
        RTP_COOLDOWN =        _RTP_COOLDOWN.loadAndSave(props).getValue();
        RTP_MAX_ATTEMPTS =    _RTP_MAX_ATTEMPTS.loadAndSave(props).getValue();
        BROADCAST_TO_OPS    = _BROADCAST_TO_OPS.loadAndSave(props).getValue();
        NICK_REVEAL_ON_HOVER= _NICK_REVEAL_ON_HOVER.loadAndSave(props).getValue();

        try {
            Objects.requireNonNull(FORMATTING_DEFAULT);
            Objects.requireNonNull(FORMATTING_ACCENT);
            Objects.requireNonNull(FORMATTING_ERROR);
        } catch (NullPointerException e) {
            EssentialCommands.log(Level.ERROR, "Something went wrong while loading chat styles from EssentialCommands.properties. Additionally, default values could not be loaded.");
            e.printStackTrace();
        }

        props.putIfAbsent(KEY_FORMATTING_DEFAULT,   String.valueOf(styleJsonDeserializer.serialize(FORMATTING_DEFAULT, null, null)));
        props.putIfAbsent(KEY_FORMATTING_ACCENT,    String.valueOf(styleJsonDeserializer.serialize(FORMATTING_ACCENT, null, null)));
        props.putIfAbsent(KEY_FORMATTING_ERROR,     String.valueOf(styleJsonDeserializer.serialize(FORMATTING_ERROR, null, null)));
        props.putIfAbsent(KEY_NICKNAME_PREFIX,      Text.Serializer.toJson(NICKNAME_PREFIX));

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
