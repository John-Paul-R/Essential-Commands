package com.fibermc.essentialcommands.types;

import java.util.HashMap;
import java.util.Optional;

import com.fibermc.essentialcommands.codec.Codecs;
import com.fibermc.essentialcommands.commands.CommandUtil;
import com.fibermc.essentialcommands.text.ECText;
import com.fibermc.essentialcommands.text.TextFormatType;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.text.Text;

public class WarpStorage extends HashMap<String, WarpLocation> implements NbtSerializable {
    public static Codec<WarpStorage> CODEC = Codecs.WARP_STORAGE;

    public WarpStorage() {}

    public WarpStorage(NbtCompound nbt) {
        this();
        loadNbt(nbt);
    }

    public static WarpStorage fromNbt(NbtCompound nbt) {
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

    public NbtCompound writeNbt(NbtCompound nbt) {
        return CODEC.encode(this, NbtOps.INSTANCE, nbt)
            .getOrThrow()
            .asCompound()
            .orElseThrow();
    }

    /**
     * @param nbt NbtCompound or NbtList. (Latter is deprecated)
     */
    private void loadNbt(NbtElement nbt) {
        if (nbt.getType() == 9) {
            // Legacy format
            NbtList homesNbtList = (NbtList) nbt;
            for (NbtElement t : homesNbtList) {
                NbtCompound homeTag = (NbtCompound) t;
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
            NbtCompound nbtCompound = (NbtCompound) nbt;
            nbtCompound.getKeys().forEach((key) -> {
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
                ECText.getInstance().getText("cmd.warp.set.error.exists", TextFormatType.Error, Text.literal(name)));
        }
    }

}
