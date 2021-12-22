package com.fibermc.essentialcommands.types;

import com.fibermc.essentialcommands.ECPerms;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Objects;

public class WarpLocation extends MinecraftLocation {

    private final String permissionString;

    /**
     * @param permissionString The string permission node for the warp. Null for no required permisison.
     */
    public WarpLocation(MinecraftLocation location, String permissionString) {
        super(
            location.dim,
            location.pos.x,
            location.pos.y,
            location.pos.z,
            location.headYaw,
            location.pitch
        );
        this.permissionString = permissionString;
    }

    public WarpLocation(NbtCompound tag) {
        super(tag);
        String permissionString1;
        permissionString1 = tag.getString("permissionString");
        if (Objects.equals(permissionString1, "")) {
            permissionString1 = null;
        }
        this.permissionString = permissionString1;
    }

    @Override
    public NbtCompound asNbt() {
        return this.writeNbt(new NbtCompound());
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        super.writeNbt(tag);
        if (permissionString != null) {
            tag.putString("permissionString", permissionString);
        }
        return tag;
    }

    public String getPermissionString() {
        return permissionString;
    }

    public boolean hasPermission(ServerPlayerEntity player) {
        return permissionString==null || ECPerms.check(
            player.getCommandSource(),
            String.format("%s.%s", ECPerms.Registry.warp_tp_named, permissionString));
    }
}
