package com.fibermc.essentialcommands.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.food.FoodData;

@Mixin(FoodData.class)
public interface FoodDataAccessor {

    @Accessor("exhaustionLevel")
    void essentialcommands$setExhaustionLevel(float exhaustionLevel);

}
