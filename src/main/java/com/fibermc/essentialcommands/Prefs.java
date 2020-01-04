package com.fibermc.essentialcommands;

import net.minecraft.util.Formatting;

import java.util.prefs.Preferences;

public class Prefs {

    public static Preferences prefs = Preferences.userNodeForPackage(com.fibermc.essentialcommands.EssentialCommands.class);

    public static String FORMATTING_DEFAULT;
    public static String FORMATTING_ACCENT;
    public static String FORMATTING_ERROR;
    public static boolean ENABLE_HOME;
    public static boolean ENABLE_TPA;
    public static boolean ENABLE_BACK;
    public static int HOME_LIMIT;
    public static double TELEPORT_COOLDOWN;
    public static double TELEPORT_DELAY;

    public static void loadPreferences() {
        FORMATTING_DEFAULT  =   prefs.get("FORMATTING_DEFAULT", "gold");
        FORMATTING_ACCENT   =   prefs.get("formatting_accent", "blue");
        FORMATTING_ERROR   =   prefs.get("formatting_error", "red");
        ENABLE_HOME         =   prefs.getBoolean("enable_home", true);
        ENABLE_TPA          =   prefs.getBoolean("enable_tpa", true);
        ENABLE_BACK         =   prefs.getBoolean("enable_back", true);
        HOME_LIMIT          =   prefs.getInt("home_limit", -1);
        TELEPORT_COOLDOWN   =   prefs.getDouble("teleport_cooldown", 1D);
        TELEPORT_DELAY      =   prefs.getDouble("teleport_delay", 0D);
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
