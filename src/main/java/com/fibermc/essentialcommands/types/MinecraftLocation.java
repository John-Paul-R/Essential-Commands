package com.fibermc.essentialcommands.types;

import com.fibermc.essentialcommands.codec.Codecs;
import com.fibermc.essentialcommands.playerdata.PlayerProfile;
import com.fibermc.essentialcommands.text.TextFormatType;

import com.mojang.serialization.Codec;

import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class MinecraftLocation {
    public static final Codec<MinecraftLocation> CODEC = Codecs.MINECRAFT_LOCATION;

    private Vec3 pos;
    private float pitch;
    private float headYaw;
    private ResourceKey<Level> dim;

    protected MinecraftLocation() {}

    public MinecraftLocation(ResourceKey<Level> dim, double x, double y, double z) {
        this.dim = dim;
        this.pos = new Vec3(x, y, z);
        this.pitch = 0f;
        this.headYaw = 0f;
        //todo world.getPersistentStateManager().
    }

    public MinecraftLocation(ResourceKey<Level> dim, double x, double y, double z, float headYaw, float pitch) {
        this.dim = dim;
        this.pos = new Vec3(x, y, z);
        this.headYaw = headYaw;
        this.pitch = pitch;
    }

    public MinecraftLocation(ResourceKey<Level> dim, Vec3i vec3i, float headYaw, float pitch) {
        this.dim = dim;
        this.pos = Vec3.atCenterOf(vec3i);
        this.headYaw = headYaw;
        this.pitch = pitch;
    }

    public MinecraftLocation(ResourceKey<Level> dim, Vec3 pos, float headYaw, float pitch) {
        this.dim = dim;
        this.pos = pos;
        this.headYaw = headYaw;
        this.pitch = pitch;
    }

    public MinecraftLocation(ServerPlayer player) {
        this.dim = player.level().dimension();
        this.pos = Vec3.ZERO.add(player.position());
        this.headYaw = player.getYHeadRot();
        this.pitch = player.getXRot();
    }

    public MinecraftLocation(CompoundTag tag) {
        this.dim = ResourceKey.create(
            Registries.DIMENSION,
            Identifier.tryParse(tag.getString("WorldRegistryKey").orElseThrow())
        );
        this.pos = new Vec3(
            tag.getDouble("x").orElseThrow(),
            tag.getDouble("y").orElseThrow(),
            tag.getDouble("z").orElseThrow()
        );
        this.headYaw = tag.getFloat("headYaw").orElse(0f);
        this.pitch = tag.getFloat("pitch").orElse(0f);
    }

    public CompoundTag asNbt() {
        return this.writeNbt(new CompoundTag());
    }

    public static MinecraftLocation fromNbt(CompoundTag tag) {
        return CODEC.parse(NbtOps.INSTANCE, tag)
            .getOrThrow();
    }

    public CompoundTag writeNbt(CompoundTag tag) {
        return CODEC.encodeStart(NbtOps.INSTANCE, this)
            .getOrThrow()
            .asCompound()
            .orElseThrow();
    }

    protected MutableComponent toLiteralTextSimple() {
        return Component.literal(String.format("(%.1f, %.1f, %.1f)", pos().x, pos().y, pos().z));
    }

    public Component toText(PlayerProfile playerProfile) {
        return toLiteralTextSimple()
            .setStyle(playerProfile.getStyle(TextFormatType.Accent));
    }

    public Vec3 pos() {
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

    public ResourceKey<Level> dim() {
        return dim;
    }
}
