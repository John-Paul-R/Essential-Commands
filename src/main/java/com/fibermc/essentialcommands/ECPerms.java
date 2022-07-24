package com.fibermc.essentialcommands;

import com.fibermc.essentialcommands.permission.NumericPermissionGroup;
import com.fibermc.essentialcommands.permission.NumericPermissionNode;
import com.fibermc.essentialcommands.permission.PermissionGroup;
import com.fibermc.essentialcommands.permission.PermissionNode;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Predicate;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;

public class ECPerms {

//    `essentialcommands.<command>.<subcommand>`
    public static final class Registry {
        public static final PermissionNode tpa                  = new PermissionNode("essentialcommands.tpa");
        public static final PermissionNode tpahere              = new PermissionNode("essentialcommands.tpahere");
        public static final PermissionNode tpaccept             = new PermissionNode("essentialcommands.tpaccept");
        public static final PermissionNode tpdeny               = new PermissionNode("essentialcommands.tpdeny");
        public static final PermissionNode home_set             = new PermissionNode("essentialcommands.home.set");
        public static final PermissionNode home_tp              = new PermissionNode("essentialcommands.home.tp");
        public static final PermissionNode home_tp_others       = new PermissionNode("essentialcommands.home_tp_others");
        public static final PermissionNode home_delete          = new PermissionNode("essentialcommands.home.delete");
        public static final PermissionNode warp_set             = new PermissionNode("essentialcommands.warp.set");
        public static final PermissionNode warp_tp              = new PermissionNode("essentialcommands.warp.tp");
        public static final PermissionNode warp_delete          = new PermissionNode("essentialcommands.warp.delete");
        public static final PermissionNode warp_tp_named        = new PermissionNode("essentialcommands.warp.tp_named");
        public static final PermissionNode back                 = new PermissionNode("essentialcommands.back");
        public static final PermissionNode spawn_tp             = new PermissionNode("essentialcommands.spawn.tp");
        public static final PermissionNode spawn_set            = new PermissionNode("essentialcommands.spawn.set");
        public static final PermissionNode nickname_self        = new PermissionNode("essentialcommands.nickname.self");
        public static final PermissionNode nickname_others      = new PermissionNode("essentialcommands.nickname.others");
        public static final PermissionNode nickname_reveal      = new PermissionNode("essentialcommands.nickname.reveal");
        public static final PermissionNode nickname_style_color = new PermissionNode("essentialcommands.nickname.style.color");
        public static final PermissionNode nickname_style_fancy = new PermissionNode("essentialcommands.nickname.style.fancy");
        public static final PermissionNode nickname_style_hover = new PermissionNode("essentialcommands.nickname.style.hover");
        public static final PermissionNode nickname_style_click = new PermissionNode("essentialcommands.nickname.style.click");
        public static final PermissionNode randomteleport       = new PermissionNode("essentialcommands.randomteleport");
        public static final PermissionNode fly_self             = new PermissionNode("essentialcommands.fly.self");
        public static final PermissionNode fly_others           = new PermissionNode("essentialcommands.fly.others");
        public static final PermissionNode invuln_self          = new PermissionNode("essentialcommands.invuln.self");
        public static final PermissionNode invuln_others        = new PermissionNode("essentialcommands.invuln.others");
        public static final PermissionNode workbench            = new PermissionNode("essentialcommands.workbench");
        public static final PermissionNode stonecutter          = new PermissionNode("essentialcommands.stonecutter");
        public static final PermissionNode grindstone           = new PermissionNode("essentialcommands.grindstone");
        public static final PermissionNode anvil                = new PermissionNode("essentialcommands.anvil");
        public static final PermissionNode enderchest           = new PermissionNode("essentialcommands.enderchest");
        public static final PermissionNode wastebin             = new PermissionNode("essentialcommands.wastebin");
        public static final PermissionNode top                  = new PermissionNode("essentialcommands.top");
        public static final PermissionNode gametime             = new PermissionNode("essentialcommands.gametime");
        public static final PermissionNode time_set_day         = new PermissionNode("essentialcommands.day");
        public static final PermissionNode afk                  = new PermissionNode("essentialcommands.afk");
        public static final PermissionNode config_reload        = new PermissionNode("essentialcommands.config.reload");
        public static final PermissionNode admin_last_pos       = new PermissionNode("essentialcommands.admin.lastpos");
        public static final PermissionNode bypass_teleport_delay                    = new PermissionNode("essentialcommands.bypass.teleport_delay");
        public static final PermissionNode bypass_allow_teleport_between_dimensions = new PermissionNode("essentialcommands.bypass.allow_teleport_between_dimensions");
        public static final PermissionNode bypass_teleport_interrupt_on_damaged     = new PermissionNode("essentialcommands.bypass.teleport_interrupt_on_damaged");
        public static final PermissionNode bypass_teleport_interrupt_on_move        = new PermissionNode("essentialcommands.bypass.teleport_interrupt_on_move");
        public static final class Group {
            public static final PermissionGroup<PermissionNode> tpa_group      = PermissionGroup.of(tpa, tpahere, tpaccept, tpdeny);
            public static final PermissionGroup<PermissionNode> home_group     = PermissionGroup.of(home_set, home_tp, home_delete);
            public static final PermissionGroup<PermissionNode> warp_group     = PermissionGroup.of(warp_set, warp_tp, warp_delete);
            public static final PermissionGroup<PermissionNode> spawn_group    = PermissionGroup.of(spawn_tp, spawn_set);
            public static final PermissionGroup<PermissionNode> nickname_group = PermissionGroup.of(nickname_self, nickname_others, nickname_reveal);
            public static final PermissionGroup<PermissionNode> fly_group      = PermissionGroup.of(fly_self, fly_others);
            public static final PermissionGroup<PermissionNode> config_group   = PermissionGroup.of(config_reload);
            public static NumericPermissionGroup home_limit_group;
        }
        public static PermissionGroup<PermissionNode> per_warp_permissions = null;
    }

    /**
     * Registers PermissionCheckEvent handler if permissions api enabled in config.
     */
    static void init() {
        var worldDataManager = ManagerLocator.getInstance().getWorldDataManager();
        Registry.per_warp_permissions = PermissionGroup.ofStrings(worldDataManager.getWarpNames().toArray(new String[0]));
        worldDataManager.WARPS_LOAD_EVENT.register((warps) -> {
            Registry.per_warp_permissions = PermissionGroup.ofStrings(warps.keySet().toArray(String[]::new));
        });
    }

    private static boolean isSuperAdmin(CommandSource source) {
        return source.hasPermissionLevel(4);
    }

    public static @NotNull Predicate<ServerCommandSource> require(@NotNull PermissionNode permission, int defaultRequireLevel) {
        return player -> check(player, permission, defaultRequireLevel);
    }

    public static @NotNull Predicate<ServerCommandSource> requireAny(@NotNull PermissionGroup<?> permissions, int defaultRequireLevel) {
        return player -> checkAny(player, permissions, defaultRequireLevel);
    }

    public static boolean check(@NotNull CommandSource source, @NotNull String permission, int defaultRequireLevel) {
        if (CONFIG.USE_PERMISSIONS_API) {
            try {
                // TODO: In the future, config option for granting ops all perms.
                return Permissions
                    .getPermissionValue(source, permission)
                    .orElse(source.hasPermissionLevel(Math.max(2, defaultRequireLevel)));
            } catch (Exception e) {
                EssentialCommands.LOGGER.error(e);
                return false;
            }
        } else {
            return source.hasPermissionLevel(defaultRequireLevel);
        }
    }

    public static boolean check(@NotNull CommandSource source, @NotNull PermissionNode permission, int defaultRequireLevel) {
        return check(source, permission.getString(), defaultRequireLevel);
    }


    public static boolean check(@NotNull CommandSource source, @NotNull String permission) {
        return check(source, permission, 4);
    }

    public static <TNode extends PermissionNode> boolean check(@NotNull CommandSource source, @NotNull TNode permission) {
        return check(source, permission, 4);
    }

    public static <TNode extends PermissionNode> boolean checkAny(@NotNull CommandSource source, @NotNull PermissionGroup<TNode> permissions, int defaultRequireLevel) {
        return permissions.streamNodes().anyMatch(permission -> check(source, permission, defaultRequireLevel));
    }

    private static int getNumericValue(String permission) {
        return Integer.parseInt(permission.substring(permission.lastIndexOf('.') + 1));
    }

    static int getHighestNumericPermission(@NotNull CommandSource source, @NotNull NumericPermissionGroup permissionGroup) {
        // No effective numeric limits for ops.
        if (isSuperAdmin(source)) {
            return Integer.MAX_VALUE;
        }

        // If ONLY -1 is present as possibility, treat as no effective limit.
        if (permissionGroup.size() == 1 && permissionGroup.getHighest().getNumericValue() == -1) {
            return Integer.MAX_VALUE;
        }

        // If permissions API is disabled, min int value in permission group is used for all non-op players.
        if (!CONFIG.USE_PERMISSIONS_API) {
            return permissionGroup.getLowest().getNumericValue();
        }

        // If permissions api is enabled, find the highest numeric permission node that the user has & return its
        // numeric value.
        var highestValue = permissionGroup
            .getHighestGranted(permission -> check(source, permission))
            .map(NumericPermissionNode::getNumericValue);

        if (highestValue.isEmpty() && CONFIG.GRANT_LOWEST_NUMERIC_BY_DEFAULT) {
            // Grant min perm value in group by default, if none are set.
            highestValue = Optional.of(permissionGroup.getLowest().getNumericValue());
        }

        // Set value to -1 in the case where the user has no relevant permissions set.
        return highestValue.orElse(-1);
    }
}