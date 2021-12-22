package com.fibermc.essentialcommands;

import me.lucko.fabric.api.permissions.v0.PermissionCheckEvent;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Predicate;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;

public class ECPerms {

    //`essentialcommands.<command>.<subcommand>`
//            `essentialcommands.<command>.*`
    public static final class Registry {
        public static final String tpa = "essentialcommands.tpa";
        public static final String tpahere = "essentialcommands.tpahere";
        public static final String tpaccept = "essentialcommands.tpaccept";
        public static final String tpdeny = "essentialcommands.tpdeny";
        public static final String home_set = "essentialcommands.home.set";
        public static final String home_tp = "essentialcommands.home.tp";
        public static final String home_delete = "essentialcommands.home.delete";
        public static final String warp_set = "essentialcommands.warp.set";
        public static final String warp_tp = "essentialcommands.warp.tp";
        public static final String warp_delete = "essentialcommands.warp.delete";
        public static final String warp_tp_named = "essentialcommands.warp.tp_named";
        public static final String back = "essentialcommands.back";
        public static final String spawn_tp = "essentialcommands.spawn.tp";
        public static final String spawn_set = "essentialcommands.spawn.set";
        public static final String nickname_self = "essentialcommands.nickname.self";
        public static final String nickname_others = "essentialcommands.nickname.others";
        public static final String nickname_reveal = "essentialcommands.nickname.reveal";
        public static final String nickname_style_color = "essentialcommands.nickname.style.color";
        public static final String nickname_style_fancy = "essentialcommands.nickname.style.fancy";
        public static final String nickname_style_hover = "essentialcommands.nickname.style.hover";
        public static final String nickname_style_click = "essentialcommands.nickname.style.click";
        public static final String randomteleport = "essentialcommands.randomteleport";
        public static final String fly_self = "essentialcommands.fly.self";
        public static final String fly_others = "essentialcommands.fly.others";
        public static final String invuln_self = "essentialcommands.invuln.self";
        public static final String invuln_others = "essentialcommands.invuln.others";
        public static final String workbench = "essentialcommands.workbench";
        public static final String enderchest = "essentialcommands.enderchest";
        public static final String top = "essentialcommands.top";
        public static final String gametime = "essentialcommands.gametime";
        public static final String config_reload = "essentialcommands.config.reload";
        public static final String bypass_teleport_delay = "essentialcommands.bypass.teleport_delay";
        public static final String bypass_allow_teleport_between_dimensions = "essentialcommands.bypass.allow_teleport_between_dimensions";
        public static final String bypass_teleport_interrupt_on_damaged = "essentialcommands.bypass.teleport_interrupt_on_damaged";
        public static final class Group {
            public static final String[] tpa_group = {tpa, tpahere, tpaccept, tpdeny};
            public static final String[] home_group = {home_set, home_tp, home_delete};
            public static final String[] warp_group = {warp_set, warp_tp, warp_delete};
            public static final String[] spawn_group = {spawn_tp, spawn_set};
            public static final String[] nickname_group = {nickname_self, nickname_others, nickname_reveal};
            public static final String[] fly_group = {fly_self, fly_others};
            public static final String[] config_group = {config_reload};
            public static String[] home_limit_group;
        }
        public static String[] per_warp_permissions = null;
    }

    /**
     * Registers PermissionCheckEvent handler if permissions api enabled in config.
     */
    static void init() {
        var worldDataManager = ManagerLocator.getInstance().getWorldDataManager();
        Registry.per_warp_permissions = worldDataManager.getWarpNames().toArray(new String[0]);
        worldDataManager.WARPS_LOAD_EVENT.register((warps) -> {
            Registry.per_warp_permissions = warps.keySet().toArray(new String[0]);
        });
        if (CONFIG.USE_PERMISSIONS_API.getValue()) {
            PermissionCheckEvent.EVENT.register((source, permission) -> {
                if (isSuperAdmin(source)) {
                    return TriState.TRUE;
                }
                return TriState.DEFAULT;
            });
        }
    }

    private static boolean isSuperAdmin(CommandSource source) {
        return source.hasPermissionLevel(4);
    }


    public static @NotNull Predicate<ServerCommandSource> require(@NotNull String permission, int defaultRequireLevel) {
        return player -> check(player, permission, defaultRequireLevel);
    }

    public static @NotNull Predicate<ServerCommandSource> requireAny(@NotNull String[] permissions, int defaultRequireLevel) {
        return player -> checkAny(player, permissions, defaultRequireLevel);
    }

    public static boolean check(@NotNull CommandSource source, @NotNull String permission, int defaultRequireLevel) {
        if (CONFIG.USE_PERMISSIONS_API.getValue()) {
            return Permissions.getPermissionValue(source, permission).orElse(false);
        } else {
            return (source.hasPermissionLevel(defaultRequireLevel) ? TriState.TRUE:TriState.FALSE).orElse(false);
        }
    }

    public static boolean check(@NotNull CommandSource source, @NotNull String permission) {
        return check(source, permission, 4);
    }

    public static boolean checkAny(@NotNull CommandSource source, @NotNull String[] permissions, int defaultRequireLevel) {
        for (String permission : permissions) {
            if (check(source, permission, defaultRequireLevel)) {
                return true;
            }
        }
        return false;
    }

    private static int getNumericValue(String permission) {
        return Integer.parseInt(permission.substring(permission.lastIndexOf('.') + 1));
    }

    static int getHighestNumericPermission(@NotNull CommandSource source, @NotNull String[] permissionGroup) {
        // No effective numeric limits for ops.
        if (isSuperAdmin(source)) {
            return Integer.MAX_VALUE;
        }

        // If ONLY -1 is present as possibility, treat as no effective limit.
        if (permissionGroup.length == 1 && getNumericValue(permissionGroup[0]) == -1) {
            return Integer.MAX_VALUE;
        }

        // If permissions API is disabled, min int value in permission group is used for all non-op players.
        if (!CONFIG.USE_PERMISSIONS_API.getValue()) {
            return Arrays.stream(permissionGroup).mapToInt(ECPerms::getNumericValue).min().getAsInt();
        }

        // If permissions api is enabled, find the highest numeric permission node that the user has & return its
        // numeric value.
        int highestValue;
        if (CONFIG.GRANT_LOWEST_NUMERIC_BY_DEFAULT.getValue()) {
            // Grant min perm value in group by default, if none are set.
            highestValue = Arrays.stream(permissionGroup).mapToInt(ECPerms::getNumericValue).min().getAsInt();
        } else {
            // Set value to -1 in the case where the user has no relevant permissions set.
            highestValue = -1;
        }
        for (String permission: permissionGroup) {
            if (check(source, permission)) {
                highestValue = Math.max(highestValue, getNumericValue(permission));
            }
        }
        return highestValue;
    }

    public static String[] makeNumericPermissionGroup(String basePermission, Collection<Integer> numericValues) {
        String trueBasePermission = basePermission.endsWith(".") ? basePermission : basePermission + ".";
        return numericValues.stream().map(el -> trueBasePermission.concat(el.toString())).toArray(String[]::new);
    }


}