package com.fibermc.essentialcommands.types;

import java.util.HashMap;

import com.fibermc.essentialcommands.commands.CommandUtil;
import com.fibermc.essentialcommands.text.ECText;
import com.fibermc.essentialcommands.text.TextFormatType;

import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.Text;

public class NamedLocationStorage extends HashMap<String, NamedMinecraftLocation> implements NbtSerializable {

    public NamedLocationStorage() {}

    public NamedLocationStorage(NbtCompound nbt) {
        this();
        loadNbt(nbt);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        this.forEach((key, value) -> nbt.put(key, value.asNbt()));
        return nbt;
    }

    /**
     * @param nbt NbtCompound or NbtList. (Latter is deprecated)
     */
    public void loadNbt(NbtElement nbt) {
        if (nbt.getType() == 9) {
            // Legacy format
            NbtList homesNbtList = (NbtList) nbt;
            for (NbtElement t : homesNbtList) {
                NbtCompound homeTag = (NbtCompound) t;
                var homeName = homeTag.getString("homeName");
                super.put(homeName, NamedMinecraftLocation.fromNbt(homeTag, homeName));
            }
        } else {
            NbtCompound nbtCompound = (NbtCompound) nbt;
            nbtCompound.getKeys().forEach((key) -> super.put(key, NamedMinecraftLocation.fromNbt(nbtCompound.getCompound(key), key)));
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
