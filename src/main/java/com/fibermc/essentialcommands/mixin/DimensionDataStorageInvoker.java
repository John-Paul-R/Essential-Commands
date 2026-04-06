package com.fibermc.essentialcommands.mixin;

import java.nio.file.Path;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.SavedDataStorage;

@Mixin(SavedDataStorage.class)
public interface DimensionDataStorageInvoker {

    @Invoker("getDataFile")
    Path invokeGetFile(Identifier id);

}
