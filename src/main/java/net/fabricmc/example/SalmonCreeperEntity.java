package net.fabricmc.example;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.world.World;

public class SalmonCreeperEntity extends CreeperEntity {

    public SalmonCreeperEntity(EntityType<SalmonCreeperEntity> type, World world) {
        super(type, world);
    }
}