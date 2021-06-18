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
            if (isSuperAdmin(source)) {
                return TriState.TRUE;
            }
            return TriState.DEFAULT;
        });
    }

    private static boolean isSuperAdmin(CommandSource source) {
        return source.hasPermissionLevel(4);
    }


    static @NotNull Predicate<ServerCommandSource> require(@NotNull String permission, int defaultRequireLevel) {
        if (Config.USE_PERMISSIONS_API) {
            return Permissions.require(permission);
        } else {
            return (ServerCommandSource source) -> source.hasPermissionLevel(defaultRequireLevel);
        }
    }

}
