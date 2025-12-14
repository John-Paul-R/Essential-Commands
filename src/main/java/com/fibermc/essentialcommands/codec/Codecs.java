package com.fibermc.essentialcommands.codec;

import java.util.HashMap;
import java.util.Optional;

import com.fibermc.essentialcommands.WorldData;
import com.fibermc.essentialcommands.types.*;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public final class Codecs {
    private Codecs() {}

    public static final Codec<ResourceKey<Level>> WORLD_KEY = ResourceKey.codec(Registries.DIMENSION);

    public static final Codec<MinecraftLocation> MINECRAFT_LOCATION = RecordCodecBuilder.create(instance ->
        instance.group(
            WORLD_KEY.fieldOf("WorldRegistryKey").forGetter(MinecraftLocation::dim),
            Codec.DOUBLE.fieldOf("x").forGetter(MinecraftLocation::x),
            Codec.DOUBLE.fieldOf("y").forGetter(MinecraftLocation::y),
            Codec.DOUBLE.fieldOf("z").forGetter(MinecraftLocation::z),
            Codec.FLOAT.optionalFieldOf("headYaw", 0.0f).forGetter(MinecraftLocation::headYaw),
            Codec.FLOAT.optionalFieldOf("pitch", 0.0f).forGetter(MinecraftLocation::pitch)
        ).apply(instance, MinecraftLocation::new)
    );

    public static final Codec<NamedMinecraftLocation> NAMED_MINECRAFT_LOCATION = RecordCodecBuilder.create(instance ->
        instance.group(
            // Inherit all fields from NamedMinecraftLocation
            WORLD_KEY.fieldOf("WorldRegistryKey").forGetter(NamedMinecraftLocation::dim),
            Codec.DOUBLE.fieldOf("x").forGetter(MinecraftLocation::x),
            Codec.DOUBLE.fieldOf("y").forGetter(MinecraftLocation::y),
            Codec.DOUBLE.fieldOf("z").forGetter(MinecraftLocation::z),
            Codec.FLOAT.optionalFieldOf("headYaw", 0.0f).forGetter(NamedMinecraftLocation::headYaw),
            Codec.FLOAT.optionalFieldOf("pitch", 0.0f).forGetter(NamedMinecraftLocation::pitch),
            // loaded from the map
            Codec.STRING.optionalFieldOf("name").forGetter(home -> Optional.of(home.getName()))

        ).apply(instance, NamedMinecraftLocation::new)
    );

    public static final Codec<WarpLocation> WARP_LOCATION = RecordCodecBuilder.create(instance ->
        instance.group(
            // Inherit all fields from NamedMinecraftLocation
            WORLD_KEY.fieldOf("WorldRegistryKey").forGetter(WarpLocation::dim),
            Codec.DOUBLE.fieldOf("x").forGetter(MinecraftLocation::x),
            Codec.DOUBLE.fieldOf("y").forGetter(MinecraftLocation::y),
            Codec.DOUBLE.fieldOf("z").forGetter(MinecraftLocation::z),
            Codec.FLOAT.optionalFieldOf("headYaw", 0.0f).forGetter(WarpLocation::headYaw),
            Codec.FLOAT.optionalFieldOf("pitch", 0.0f).forGetter(WarpLocation::pitch),
            // loaded from the map
            Codec.STRING.optionalFieldOf("name").forGetter(warp -> Optional.of(warp.getName())),

            Codec.STRING.optionalFieldOf("permissionString").forGetter(warp -> Optional.ofNullable(warp.getPermissionString()))

        ).apply(instance, WarpLocation::new)
    );

    public static final Codec<NamedLocationStorage> NAMED_LOCATION_STORAGE =
        Codec.unboundedMap(Codec.STRING, NAMED_MINECRAFT_LOCATION)
            .xmap(
                // Convert Map to NamedLocationStorage
                map -> {
                    NamedLocationStorage storage = new NamedLocationStorage();
                    map.forEach(
                        (key, value) -> storage.put(key, new NamedMinecraftLocation(value, key))
                    );
                    return storage;
                },
                // Convert NamedLocationStorage to Map for serialization
                HashMap::new
            );

    public static final Codec<WarpStorage> WARP_STORAGE =
        Codec.unboundedMap(Codec.STRING, WARP_LOCATION)
            .xmap(
                // Convert Map to WarpStorage
                map -> {
                    WarpStorage storage = new WarpStorage();
                    map.forEach(
                        (key, value) -> storage.put(key, WarpLocation.setName(value, key))
                    );
                    return storage;
                },
                // Convert WarpStorage to Map for serialization
                HashMap::new
            );

    public static final Codec<WorldData> WORLD_DATA = WorldData.CODEC;
}
