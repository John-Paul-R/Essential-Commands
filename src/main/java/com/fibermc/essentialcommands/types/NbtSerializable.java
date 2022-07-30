package com.fibermc.essentialcommands.types;

import net.minecraft.nbt.NbtCompound;

public interface NbtSerializable {

    NbtCompound writeNbt(NbtCompound tag);
}
