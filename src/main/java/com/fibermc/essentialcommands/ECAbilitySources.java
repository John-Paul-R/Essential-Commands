package com.fibermc.essentialcommands;

import io.github.ladysnake.pal.AbilitySource;
import io.github.ladysnake.pal.Pal;

public class ECAbilitySources {
    public static final AbilitySource FLY_COMMAND = Pal.getAbilitySource(EssentialCommands.MOD_ID, "ec-fly-command");
    public static final AbilitySource INVULN_COMMAND = Pal.getAbilitySource(EssentialCommands.MOD_ID, "ec-invuln-command");
    public static final AbilitySource AFK_INVULN = Pal.getAbilitySource(EssentialCommands.MOD_ID, "ec-afk-invuln");
}
