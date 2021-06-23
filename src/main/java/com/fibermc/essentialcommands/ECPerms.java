package com.fibermc.essentialcommands;

import me.lucko.fabric.api.permissions.v0.PermissionCheckEvent;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.util.TriState;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.ServerCommandSource;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public class ECPerms {

    static void init() {
        PermissionCheckEvent.EVENT.register((source, permission) -> {
//            if (isSuperAdmin(source)) {
//                return TriState.TRUE;
//            }
            return TriState.DEFAULT;
        });
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
