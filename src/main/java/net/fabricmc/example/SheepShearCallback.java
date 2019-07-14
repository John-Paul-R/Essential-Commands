/*
 * Callback for shearing a sheep.
 * Called before the sheep is sheared, items are dropped, and items are damaged.
 * Upon return:
 * - SUCCESS cancels further processing and continues with normal shearing behavior.
 * - PASS falls back to further processing and defaults to SUCCESS if no other listeners are available
 * - FAIL cancels further processing and does not shear the sheep.
*/
package net.fabricmc.example;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;

public interface SheepShearCallback
{
    Event<SheepShearCallback> EVENT = EventFactory.createArrayBacked(SheepShearCallback.class,
        (listeners) -> (player, sheep) -> {
            for (SheepShearCallback event : listeners) {
                ActionResult result = event.interact(player, sheep);
                if(result != ActionResult.PASS) {
                    return result;
                }
            }
 
        return ActionResult.PASS;
    });
 
    ActionResult interact(PlayerEntity player, SheepEntity sheep);
}