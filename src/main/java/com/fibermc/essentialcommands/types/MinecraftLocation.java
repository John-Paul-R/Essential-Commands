package com.fibermc.essentialcommands.types;

import com.fibermc.essentialcommands.playerdata.PlayerProfile;
import com.fibermc.essentialcommands.text.TextFormatType;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

public class MinecraftLocation {

    private Vec3d pos;
    private float pitch;
    private float headYaw;
    private RegistryKey<World> dim;

    protected MinecraftLocation() {}

    public MinecraftLocation(RegistryKey<World> dim, double x, double y, double z) {
        this.dim = dim;
        this.pos = new Vec3d(x, y, z);
        this.pitch = 0f;
        this.headYaw = 0f;
        //todo world.getPersistentStateManager().
    }

    public MinecraftLocation(RegistryKey<World> dim, double x, double y, double z, float headYaw, float pitch) {
        this.dim = dim;
        this.pos = new Vec3d(x, y, z);
        this.headYaw = headYaw;
        this.pitch = pitch;
    }

    public MinecraftLocation(RegistryKey<World> dim, Vec3i vec3i, float headYaw, float pitch) {
        this.dim = dim;
        this.pos = Vec3d.ofCenter(vec3i);
        this.headYaw = headYaw;
        this.pitch = pitch;
    }

    public MinecraftLocation(ServerPlayerEntity player) {
        this.dim = player.getWorld().getRegistryKey();
        this.pos = Vec3d.ZERO.add(player.getPos());
        this.headYaw = player.getHeadYaw();
        this.pitch = player.getPitch();
    }

    public MinecraftLocation(NbtCompound tag) {
        this.dim = RegistryKey.of(
            RegistryKeys.WORLD,
            Identifier.tryParse(tag.getString("WorldRegistryKey"))
        );
        this.pos = new Vec3d(
            tag.getDouble("x"),
            tag.getDouble("y"),
            tag.getDouble("z")
        );
        this.headYaw = tag.getFloat("headYaw");
        this.pitch = tag.getFloat("pitch");
    }

    public static MinecraftLocation fromNbt(NbtCompound tag) {
        var loc = new MinecraftLocation();
        loc.loadNbt(tag);
        return loc;
    }

    protected void loadNbt(NbtCompound tag) {
        this.dim = RegistryKey.of(
            RegistryKeys.WORLD,
            Identifier.tryParse(tag.getString("WorldRegistryKey"))
        );
        this.pos = new Vec3d(
            tag.getDouble("x"),
            tag.getDouble("y"),
            tag.getDouble("z")
        );
        this.headYaw = tag.getFloat("headYaw");
        this.pitch = tag.getFloat("pitch");
    }

    public NbtCompound asNbt() {
        return this.writeNbt(new NbtCompound());
    }

    public NbtCompound writeNbt(NbtCompound tag) {
        tag.putString("WorldRegistryKey", dim().getValue().toString());
        tag.putDouble("x", pos().x);
        tag.putDouble("y", pos().y);
        tag.putDouble("z", pos().z);
        tag.putFloat("headYaw", headYaw());
        tag.putFloat("pitch", pitch());

        return tag;
    }

    protected MutableText toLiteralTextSimple() {
        return Text.literal(String.format("(%.1f, %.1f, %.1f)", pos().x, pos().y, pos().z));
    }

    public Text toText(PlayerProfile playerProfile) {
        return toLiteralTextSimple()
            .setStyle(playerProfile.getStyle(TextFormatType.Accent));
    }

    public Vec3d pos() {
        return pos;
    }

    public Vec3i intPos() {
        return new Vec3i((int) pos.x, (int) pos.y, (int) pos.z);
    }

    public float pitch() {
        return pitch;
    }

    public float headYaw() {
        return headYaw;
    }

    public RegistryKey<World> dim() {
        return dim;
    }
}
