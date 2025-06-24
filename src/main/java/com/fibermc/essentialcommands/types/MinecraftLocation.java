package com.fibermc.essentialcommands.types;

import com.fibermc.essentialcommands.codec.Codecs;
import com.fibermc.essentialcommands.playerdata.PlayerProfile;
import com.fibermc.essentialcommands.text.TextFormatType;

import com.mojang.serialization.Codec;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
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
    public static final Codec<MinecraftLocation> CODEC = Codecs.MINECRAFT_LOCATION;

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

    public MinecraftLocation(RegistryKey<World> dim, Vec3d pos, float headYaw, float pitch) {
        this.dim = dim;
        this.pos = pos;
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
            Identifier.tryParse(tag.getString("WorldRegistryKey").orElseThrow())
        );
        this.pos = new Vec3d(
            tag.getDouble("x").orElseThrow(),
            tag.getDouble("y").orElseThrow(),
            tag.getDouble("z").orElseThrow()
        );
        this.headYaw = tag.getFloat("headYaw").orElse(0f);
        this.pitch = tag.getFloat("pitch").orElse(0f);
    }

    public NbtCompound asNbt() {
        return this.writeNbt(new NbtCompound());
    }

    public static MinecraftLocation fromNbt(NbtCompound tag) {
        return CODEC.parse(NbtOps.INSTANCE, tag)
            .getOrThrow();
    }

    public NbtCompound writeNbt(NbtCompound tag) {
        return CODEC.encodeStart(NbtOps.INSTANCE, this)
            .getOrThrow()
            .asCompound()
            .orElseThrow();
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

    public double x() {
        return pos.x;
    }

    public double y() {
        return pos.y;
    }

    public double z() {
        return pos.z;
    }

    public RegistryKey<World> dim() {
        return dim;
    }
}
