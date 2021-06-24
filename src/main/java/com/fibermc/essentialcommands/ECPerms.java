package com.fibermc.essentialcommands;

import me.lucko.fabric.api.permissions.v0.PermissionCheckEvent;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public class ECPerms {

//`essentialcommands.<command>.<subcommand>`
//            `essentialcommands.<command>.*`
    public static final class Registry {
        public static final String tpa = "essentialcommands.tpa";
        public static final String tpaccept = "essentialcommands.tpaccept";
        public static final String tpdeny = "essentialcommands.tpdeny";
        public static final String home_set = "essentialcommands.home.set";
        public static final String home_tp = "essentialcommands.home.tp";
        public static final String home_delete = "essentialcommands.home.delete";
        public static final String warp_set = "essentialcommands.warp.set";
        public static final String warp_tp = "essentialcommands.warp.tp";
        public static final String warp_delete = "essentialcommands.warp.delete";
        public static final String back = "essentialcommands.back";
        public static final String spawn_tp = "essentialcommands.spawn.tp";
        public static final String spawn_set = "essentialcommands.spawn.set";
        public static final String nickname_set =   "essentialcommands.nickname.set";
        public static final String config_reload = "essentialcommands.config.reload";
        public static final String bypass_teleport_delay = "essentialcommands.bypass.teleport_delay";
        public static final String bypass_allow_teleport_between_dimensions = "essentialcommands.bypass.allow_teleport_between_dimensions";
        public static final String bypass_teleport_interrupt_on_damaged = "essentialcommands.bypass.teleport_interrupt_on_damaged";
    }

    static void init() {
        if (Config.USE_PERMISSIONS_API) {
            PermissionCheckEvent.EVENT.register((source, permission) -> {
//                if (isSuperAdmin(source)) {
//                    return TriState.TRUE;
//                }
                return TriState.DEFAULT;
            });
        }
    }

    private static boolean isSuperAdmin(CommandSource source) {
        return source.hasPermissionLevel(4);
    }


    static @NotNull Predicate<ServerCommandSource> require(@NotNull String permission, int defaultRequireLevel) {
        return player -> check(player, permission, defaultRequireLevel);
    }

    static boolean check(@NotNull CommandSource source, @NotNull String permission, int defaultRequireLevel) {
        if (Config.USE_PERMISSIONS_API) {
            return Permissions.getPermissionValue(source, permission).orElse(false);
        } else {
            return (source.hasPermissionLevel(defaultRequireLevel) ? TriState.TRUE : TriState.FALSE).orElse(false);
        }
    }

    static boolean check(@NotNull CommandSource source, @NotNull String permission) {
        return check(source, permission, 4);
    }
}
