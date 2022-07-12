package com.fibermc.essentialcommands;

import io.github.ladysnake.pal.AbilitySource;
import io.github.ladysnake.pal.Pal;

public final class ECAbilitySources {
    // Call this on startup.
    // Forces clinit, so that ability sources get registered with Pal.
    // If a player joins rejoins the server with an EC ability active before
    // ECAbilitySources clinit has occurred, Pal will disable the ability, as it
    // does not recognize the (not yet registered source).
    public static void init() {};
    public static final AbilitySource FLY_COMMAND = Pal.getAbilitySource(EssentialCommands.MOD_ID, "ec-fly-command");
    public static final AbilitySource INVULN_COMMAND = Pal.getAbilitySource(EssentialCommands.MOD_ID, "ec-invuln-command");
    public static final AbilitySource AFK_INVULN = Pal.getAbilitySource(EssentialCommands.MOD_ID, "ec-afk-invuln");
}
