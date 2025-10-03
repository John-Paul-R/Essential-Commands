package com.fibermc.joinpoints;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.fibermc.joinpoints.types.JoinpointLimit;
import me.lucko.fabric.api.permissions.v0.Permissions;
import org.jetbrains.annotations.NotNull;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public final class JoinpointsPerms {
    private JoinpointsPerms() {}

    @SuppressWarnings({"checkstyle:constantname", "checkstyle:staticvariablename"})
    public static final class Registry {
        public static final String joinpoint_set = "joinpoints.set";
        public static final String joinpoint_tp = "joinpoints.tp";
        public static final String joinpoint_delete = "joinpoints.delete";

        public static final class Group {
            public static final String[] joinpoint_group = {joinpoint_set, joinpoint_tp, joinpoint_delete};
            public static Map<JoinpointLimit.JoinpointType, String[]> joinpoint_limit_groups = new HashMap<>();
        }
    }

    public static Predicate<ServerCommandSource> require(String permission, int opLevel) {
        return Permissions.require(permission, opLevel);
    }

    public static Predicate<ServerCommandSource> requireAny(String[] permissions, int opLevel) {
        return src -> Arrays.stream(permissions).anyMatch(perm -> Permissions.check(src, perm, opLevel));
    }

    public static String[] makeNumericPermissionGroup(String basePermission, @NotNull Collection<Integer> limits) {
        return limits.stream()
            .map(limit -> basePermission + "." + limit)
            .toArray(String[]::new);
    }

    public static int getMaximumNumericalPermission(ServerPlayerEntity player, String basePermission, String[] validPermissions) {
        return Stream.of(validPermissions)
            .filter(perm -> Permissions.check(player, basePermission + "." + perm))
            .map(Integer::parseInt)
            .max(Integer::compare)
            .orElse(0);
    }

    // Delegate to ECPerms for the shared permission logic
    public static int getHighestNumericPermission(@NotNull ServerCommandSource source, @NotNull String[] permissionGroup) {
        return com.fibermc.essentialcommands.ECPerms.getHighestNumericPermission(source, permissionGroup);
    }
}
