package com.fibermc.essentialcommands.mixin;

import java.io.File;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.PersistentStateManager;

@Mixin(PersistentStateManager.class)
public interface PersistentStateManagerInvoker {

    @Invoker("getFile")
    File invokeGetFile(String id);

}
