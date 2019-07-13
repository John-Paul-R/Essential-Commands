package net.fabricmc.example;

import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.util.Identifier;

public class SalmonCreeperRenderer
        extends MobEntityRenderer<SalmonCreeperEntity, SalmonCreeperModel<SalmonCreeperEntity>> {

    public SalmonCreeperRenderer(EntityRenderDispatcher entityRenderDispatcher_1) {
        super(entityRenderDispatcher_1, new SalmonCreeperModel<SalmonCreeperEntity>(), 1);
    }
    
    @Override
    protected Identifier getTexture(SalmonCreeperEntity var1) {
        return new Identifier("essential_commands:textures/entity/cookie_creeper/salmon.png");
    }
}