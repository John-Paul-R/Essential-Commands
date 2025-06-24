package com.fibermc.essentialcommands.types;

import java.util.HashMap;

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

public class NamedLocationStorage extends HashMap<String, NamedMinecraftLocation> implements NbtSerializable {
    public static final Codec<NamedLocationStorage> CODEC = Codecs.NAMED_LOCATION_STORAGE;

    public NamedLocationStorage() {}

    public NamedLocationStorage(NbtCompound nbt) {
        this();
        loadNbt(nbt);
    }

    public static NamedLocationStorage fromNbt(NbtCompound nbt) {
        // Try codec first
        var result = CODEC.parse(NbtOps.INSTANCE, nbt);
        if (result.isSuccess()) {
            return result.getOrThrow();
        }

        // Fallback to legacy parsing
        NamedLocationStorage storage = new NamedLocationStorage();
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
     * Legacy NBT loading method - supports both old list format and compound format
     * @param nbt NbtCompound or NbtList. (NbtList is deprecated)
     */
    private void loadNbt(NbtElement nbt) {
        if (nbt.getType() == 9) {
            // Legacy format - NbtList
            NbtList homesNbtList = (NbtList) nbt;
            for (NbtElement t : homesNbtList) {
                NbtCompound homeTag = (NbtCompound) t;
                homeTag.getString("homeName").ifPresent((homeName) -> {
                    var location = MinecraftLocation.fromNbt(homeTag);
                    super.put(homeName, new NamedMinecraftLocation(location, homeName));
                });
            }
        } else {
            // Legacy compound format
            NbtCompound nbtCompound = (NbtCompound) nbt;
            nbtCompound.getKeys().forEach((key) -> {
                var location = NamedMinecraftLocation.fromNbt(nbtCompound.getCompound(key).orElseThrow());
                if (!key.equals(location.getName())) {
                    throw new RuntimeException("Home key '%s' did not match home name '%s'".formatted(key, location.getName()));
                }
                super.put(key, location);
            });
        }
    }

    public MinecraftLocation putCommand(String name, MinecraftLocation location) throws CommandSyntaxException {
        return putCommand(name, new NamedMinecraftLocation(location, name));
    }

    private MinecraftLocation putCommand(String name, NamedMinecraftLocation location) throws CommandSyntaxException {
        if (this.get(name) == null) {
            return super.put(name, location);
        } else {
            throw CommandUtil.createSimpleException(
                ECText.getInstance().getText("cmd.home.set.error.exists", TextFormatType.Error, Text.literal(name)));
        }
    }

}
