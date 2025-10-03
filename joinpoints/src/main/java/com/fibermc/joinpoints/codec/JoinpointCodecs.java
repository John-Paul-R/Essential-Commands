package com.fibermc.joinpoints.codec;

import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import com.fibermc.essentialcommands.types.MinecraftLocation;
import com.fibermc.joinpoints.types.JoinpointLocation;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.World;

public final class JoinpointCodecs {
    private JoinpointCodecs() {}

    public static final Codec<RegistryKey<World>> WORLD_KEY = RegistryKey.createCodec(RegistryKeys.WORLD);

    public static final Codec<JoinpointLocation> JOINPOINT_LOCATION = RecordCodecBuilder.create(instance ->
        instance.group(
            // Inherit all fields from NamedMinecraftLocation
            WORLD_KEY.fieldOf("WorldRegistryKey").forGetter(JoinpointLocation::dim),
            Codec.DOUBLE.fieldOf("x").forGetter(MinecraftLocation::x),
            Codec.DOUBLE.fieldOf("y").forGetter(MinecraftLocation::y),
            Codec.DOUBLE.fieldOf("z").forGetter(MinecraftLocation::z),
            Codec.FLOAT.optionalFieldOf("headYaw", 0.0f).forGetter(JoinpointLocation::headYaw),
            Codec.FLOAT.optionalFieldOf("pitch", 0.0f).forGetter(JoinpointLocation::pitch),
            // loaded from the map
            Codec.STRING.optionalFieldOf("name").forGetter(joinpoint -> Optional.of(((JoinpointLocation)joinpoint).getName())),

            // Joinpoint-specific fields
            Codec.STRING.xmap(UUID::fromString, UUID::toString).fieldOf("owner").forGetter(JoinpointLocation::getOwner),
            Codec.BOOL.optionalFieldOf("isGlobal", false).forGetter(JoinpointLocation::isGlobal),
            Codec.STRING.xmap(UUID::fromString, UUID::toString).listOf().optionalFieldOf("sharedWith", java.util.List.of())
                .xmap(HashSet::new, java.util.List::copyOf)
                .forGetter(JoinpointLocation::getSharedWith)

        ).apply(instance, JoinpointLocation::new)
    );
}
