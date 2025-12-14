package com.fibermc.essentialcommands.types;

import net.minecraft.nbt.CompoundTag;

public interface NbtSerializable {

    CompoundTag writeNbt(CompoundTag tag);
}
