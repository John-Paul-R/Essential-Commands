package com.fibermc.essentialcommands;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Stream;

import net.minecraft.resources.Identifier;

import org.jetbrains.annotations.NotNull;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;

public final class ECPerms {
    private ECPerms() {}

    //`essentialcommands.<command>.<subcommand>`
//            `essentialcommands.<command>.*`
    @SuppressWarnings({"checkstyle:constantname", "checkstyle:staticvariablename"})
    public static final class Registry {
        public static final Identifier tpa = of("tpa");
        public static final Identifier tpahere = of("tpahere");
        public static final Identifier tpaccept = of("tpaccept");
        public static final Identifier tpdeny = of("tpdeny");
        public static final Identifier home_set = of("home.set");
        public static final Identifier home_tp = of("home.tp");
        public static final Identifier home_tp_others = of("home_tp_others");
        public static final Identifier home_delete = of("home.delete");
        public static final Identifier warp_set = of("warp.set");
        public static final Identifier warp_tp = of("warp.tp");
        public static final Identifier warp_delete = of("warp.delete");
        public static final Identifier warp_tp_named = of("warp.tp_named");
        public static final Identifier warp_tp_others = of("warp_tp_others");
        public static final Identifier back = of("back");
        public static final Identifier spawn_tp = of("spawn.tp");
        public static final Identifier spawn_set = of("spawn.set");
        public static final Identifier nickname_self = of("nickname.self");
        public static final Identifier nickname_others = of("nickname.others");
        public static final Identifier nickname_reveal = of("nickname.reveal");
        public static final Identifier nickname_style_color = of("nickname.style.color");
        public static final Identifier nickname_style_fancy = of("nickname.style.fancy");
        public static final Identifier nickname_style_hover = of("nickname.style.hover");
        public static final Identifier nickname_style_click = of("nickname.style.click");
        public static final Identifier nickname_selector_and_ctx = of("nickname.style.selector_and_context");
        public static final Identifier nickname_placeholders = of("nickname.placeholders");
        public static final Identifier randomteleport = of("randomteleport");
        public static final Identifier fly_self = of("fly.self");
        public static final Identifier fly_others = of("fly.others");
        public static final Identifier invuln_self = of("invuln.self");
        public static final Identifier invuln_others = of("invuln.others");
        public static final Identifier workbench = of("workbench");
        public static final Identifier stonecutter = of("stonecutter");
        public static final Identifier grindstone = of("grindstone");
        public static final Identifier anvil = of("anvil");
        public static final Identifier enderchest = of("enderchest");
        public static final Identifier wastebin = of("wastebin");
        public static final Identifier top = of("top");
        public static final Identifier gametime = of("gametime");
        public static final Identifier time_set_day = of("day");
        public static final Identifier afk = of("afk");
        public static final Identifier bed = of("bed");
        public static final Identifier sleep = of("sleep");
        public static final Identifier config_reload = of("config.reload");
        public static final Identifier bypass_teleport_delay = of("bypass.teleport_delay");
        public static final Identifier bypass_allow_teleport_between_dimensions = of("bypass.allow_teleport_between_dimensions");
        public static final Identifier bypass_teleport_interrupt_on_damaged = of("bypass.teleport_interrupt_on_damaged");
        public static final Identifier bypass_teleport_interrupt_on_move = of("bypass.teleport_interrupt_on_move");
        public static final Identifier bypass_randomteleport_cooldown = of("bypass.randomteleport_cooldown");
        public static final Identifier rules_reload = of("rules_reload");
        public static final Identifier rules = of("rules");
        public static final Identifier feed_self = of("feed.self");
        public static final Identifier feed_others = of("feed.others");
        public static final Identifier heal_self = of("heal.self");
        public static final Identifier heal_others = of("heal.others");
        public static final Identifier extinguish_self = of("extinguish.self");
        public static final Identifier extinguish_others = of("extinguish.others");
        public static final Identifier suicide = of("suicide");
        public static final Identifier time_set_night = of("night");
        public static final Identifier repair_self = of("repair.self");
        public static final Identifier repair_others = of("repair.others");
        public static final Identifier near_self = of("near.self");
        public static final Identifier near_others = of("near.others");
        public static final Identifier motd = of("motd");
        public static final Identifier admin_lastpos = of("admin.lastpos");

        public static final class Group {
            public static final Identifier[] tpa_group = {tpa, tpahere, tpaccept, tpdeny};
            public static final Identifier[] home_group = {home_set, home_tp, home_delete};
            public static final Identifier[] warp_group = {warp_set, warp_tp, warp_delete};
            public static final Identifier[] spawn_group = {spawn_tp, spawn_set};
            public static final Identifier[] nickname_group = {nickname_self, nickname_others, nickname_reveal};
            public static final Identifier[] fly_group = {fly_self, fly_others};
            public static final Identifier[] invuln_group = {invuln_self, invuln_others};
            public static final Identifier[] config_group = {config_reload};
            public static Identifier[] home_limit_group;
            public static final Identifier[] stateful_player_abilities = {fly_self, fly_others, invuln_self, invuln_others};
        }

        public static String[] per_warp_permissions = null;
    }

    /**
     * Registers PermissionCheckEvent handler if permissions api enabled in config.
     */
    static void init() {
        var worldDataManager = ManagerLocator.getInstance().getWorldDataManager();
        Registry.per_warp_permissions = worldDataManager.getWarpNames().toArray(String[]::new);
        worldDataManager.warpsLoadEvent.register((warps) -> {
            Registry.per_warp_permissions = warps.keySet().toArray(String[]::new);
        });
    }

    public static Identifier of(String name) {
        return Identifier.fromNamespaceAndPath("essentialcommands", name);
    }

    private static boolean isSuperAdmin(CommandSourceStack source) {
        return source.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.OWNERS));
    }

    public static @NotNull Predicate<CommandSourceStack> require(@NotNull Identifier permission, int defaultRequireLevel) {
        return player -> check(player, permission, defaultRequireLevel);
    }

    public static @NotNull Predicate<CommandSourceStack> requireAny(@NotNull Identifier[] permissions, int defaultRequireLevel) {
        return player -> checkAny(player, permissions, defaultRequireLevel);
    }

    public static boolean check(@NotNull CommandSourceStack source, @NotNull Identifier permission, int defaultRequireLevel) {
        if (CONFIG.USE_PERMISSIONS_API) {
            try {
                // TODO: In the future, config option for granting ops all perms.
                if (defaultRequireLevel > 4) {
                    return source.checkPermission(permission, false);
                }
                return source.checkPermission(permission, PermissionLevel.byId(defaultRequireLevel));
            } catch (Exception e) {
                EssentialCommands.LOGGER.error(e);
                return false;
            }
        } else {
            return source.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.byId(defaultRequireLevel)));
        }
    }

    public static boolean check(@NotNull CommandSourceStack source, @NotNull Identifier permission) {
        return check(source, permission, 4);
    }

    public static boolean checkAny(@NotNull CommandSourceStack source, @NotNull Identifier[] permissions, int defaultRequireLevel) {
        for (Identifier permission : permissions) {
            if (check(source, permission, defaultRequireLevel)) {
                return true;
            }
        }
        return false;
    }

    private static int getNumericValue(Identifier permission) {
        String permissionString = getPermissionString(permission);
        return Integer.parseInt(permissionString.substring(permissionString.lastIndexOf('.') + 1));
    }

    public static int getHighestNumericPermission(@NotNull CommandSourceStack source, @NotNull Identifier[] permissionGroup) {
        // No effective numeric limits for ops.
        if (isSuperAdmin(source)) {
            return Integer.MAX_VALUE;
        }

        // If ONLY -1 is present as possibility, treat as no effective limit.
        if (permissionGroup.length == 1 && getNumericValue(permissionGroup[0]) == -1) {
            return Integer.MAX_VALUE;
        }

        // If permissions API is disabled, min int value in permission group is used for all non-op players.
        if (!CONFIG.USE_PERMISSIONS_API) {
            return Arrays.stream(permissionGroup).mapToInt(ECPerms::getNumericValue).min().getAsInt();
        }

        // If permissions api is enabled, find the highest numeric permission node that the user has & return its
        // numeric value.
        int highestValue;
        if (CONFIG.GRANT_LOWEST_NUMERIC_BY_DEFAULT) {
            // Grant min perm value in group by default, if none are set.
            highestValue = Arrays.stream(permissionGroup).mapToInt(ECPerms::getNumericValue).min().getAsInt();
        } else {
            // Set value to -1 in the case where the user has no relevant permissions set.
            highestValue = -1;
        }
        for (Identifier permission : permissionGroup) {
            if (check(source, permission)) {
                highestValue = Math.max(highestValue, getNumericValue(permission));
            }
        }
        return highestValue;
    }

    public static Identifier[] makeNumericPermissionGroup(Identifier basePermission, Collection<Integer> numericValues) {
        Identifier trueBasePermission = basePermission.getPath().endsWith(".") ? basePermission : basePermission.withSuffix(".");
        return numericValues.stream().map(el -> trueBasePermission.withSuffix(el.toString())).toArray(Identifier[]::new);
    }

    public static Stream<Identifier> getGrantedStatefulPlayerAbilityPermissions(ServerPlayer player) {
        var list = Arrays.stream(Registry.Group.stateful_player_abilities);
        return player.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.GAMEMASTERS))
            ? list // TODO: this is hacky
            : list.filter(permission -> check(player.createCommandSourceStack(), permission));
    }

    private static String getPermissionString(Identifier permission) {
        return permission.getNamespace() + "." + permission.getPath();
    }
}
