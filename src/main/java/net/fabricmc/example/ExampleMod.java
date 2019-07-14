package net.fabricmc.example;

/* import java.util.function.Consumer;

import com.mojang.brigadier.CommandDispatcher; */

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.render.EntityRendererRegistry;
import net.fabricmc.fabric.api.entity.FabricEntityTypeBuilder;
import net.minecraft.client.MinecraftClient;
/* import net.fabricmc.fabric.api.registry.CommandRegistry;
 */import net.minecraft.entity.EntityCategory;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
/* import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource; */
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;



public class ExampleMod implements ModInitializer {
    
    /* class TestConsumer implements Consumer<CommandDispatcher<ServerCommandSource>> {
        @Override
        public void accept(CommandDispatcher<ServerCommandSource> t) {
            CommandOutput o = MinecraftServer.DUMMY;
        };
        
            
            //ServerCommandSource s = new ServerCommandSource(o, new Vec3d(), new Vec2f(0, 0), MinecraftServer, int_1, string_1, text_1, minecraftServer_1, entity_1)
            CommandDispatcher<ServerCommandSource> summon = new CommandDispatcher<ServerCommandSource>();
            //summon.parse(command, s)
            //SummonCommand.register();
    } */



    @Override
	public void onInitialize() {
        
        //CommandRegistry.INSTANCE.register(false, new TestConsumer());
        Registry.register(
            Registry.ENTITY_TYPE,
            new Identifier("essential_commands", "cookie-creeper"),
            FabricEntityTypeBuilder.create(EntityCategory.AMBIENT, SalmonCreeperEntity::new).size(EntityDimensions.fixed( .7f, 0.4f)).build()
        );



        SheepShearCallback.EVENT.register((player, sheep) ->
        {
            //sheep.setSheared(true);
        
            MinecraftClient client = MinecraftClient.getInstance();
            client.isHudEnabled();
            client.gameRenderer.firstPersonRenderer.renderOverlays(1f);
            client.inGameHud;

            // create diamond item entity at sheep position
            ItemStack stack = new ItemStack(Items.DIAMOND, 64);
            ItemEntity itemEntity = new ItemEntity(player.world, sheep.x, sheep.y, sheep.z, stack);
            player.world.spawnEntity(itemEntity);
        
            
            return ActionResult.PASS;
        });

        EntityRendererRegistry.INSTANCE.register(SalmonCreeperEntity.class, (entityRenderDispatcher, context) -> new SalmonCreeperRenderer(entityRenderDispatcher));
        
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		System.out.println("Hello Fabric world!");
	}
}
