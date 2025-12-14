package com.fibermc.essentialcommands.types;

import java.util.HashMap;
import java.util.Optional;

import com.fibermc.essentialcommands.codec.Codecs;
import com.fibermc.essentialcommands.commands.CommandUtil;
import com.fibermc.essentialcommands.text.ECText;
import com.fibermc.essentialcommands.text.TextFormatType;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;

public class WarpStorage extends HashMap<String, WarpLocation> implements NbtSerializable {
    @SuppressWarnings({"checkstyle:StaticVariableName"})
    public static Codec<WarpStorage> CODEC = Codecs.WARP_STORAGE;

    public WarpStorage() {}

    public WarpStorage(CompoundTag nbt) {
        this();
        loadNbt(nbt);
    }

    public static WarpStorage fromNbt(CompoundTag nbt) {
        // Try codec first
        var result = CODEC.parse(NbtOps.INSTANCE, nbt);
        if (result.isSuccess()) {
            return result.getOrThrow();
        }

        // Fallback to legacy parsing
        WarpStorage storage = new WarpStorage();
        storage.loadNbt(nbt);
        return storage;
    }

    public CompoundTag writeNbt(CompoundTag nbt) {
        return CODEC.encode(this, NbtOps.INSTANCE, nbt)
            .getOrThrow()
            .asCompound()
            .orElseThrow();
    }

    /**
     * @param nbt NbtCompound or NbtList. (Latter is deprecated)
     */
    private void loadNbt(Tag nbt) {
        if (nbt.getId() == 9) {
            // Legacy format
            ListTag homesNbtList = (ListTag) nbt;
            for (Tag t : homesNbtList) {
                CompoundTag homeTag = (CompoundTag) t;
                String name = homeTag.getString("homeName").orElseThrow();
                var location = MinecraftLocation.fromNbt(homeTag);
                super.put(
                    name,
                    new WarpLocation(
                        location,
                        homeTag.getString("permissionString")
                            .flatMap(str -> str.isBlank() ? Optional.empty() : Optional.of(str))
                            .orElse(null),
                        name
                    )
                );
            }
        } else {
            CompoundTag nbtCompound = (CompoundTag) nbt;
            nbtCompound.keySet().forEach((key) -> {
                var location = WarpLocation.fromNbt(nbtCompound.getCompound(key).orElseThrow());
                if (!key.equals(location.getName())) {
                    throw new RuntimeException("Warp key '%s' did not match home name '%s'".formatted(key, location.getName()));
                }
                super.put(key, location);
            });
        }

    }

    public WarpLocation putCommand(String name, WarpLocation location) throws CommandSyntaxException {
        if (this.get(name) == null) {
            return super.put(name, location);
        } else {
            throw CommandUtil.createSimpleException(
                ECText.getInstance().getText("cmd.warp.set.error.exists", TextFormatType.Error, Component.literal(name)));
        }
    }

}
