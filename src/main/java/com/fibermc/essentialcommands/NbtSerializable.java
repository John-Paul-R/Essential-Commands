package com.fibermc.essentialcommands;

import net.minecraft.nbt.NbtCompound;

public interface NbtSerializable {

    NbtCompound writeNbt(NbtCompound tag);
}
