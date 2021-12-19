package com.fibermc.essentialcommands;

import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import eu.pb4.placeholders.PlaceholderAPI;
import eu.pb4.placeholders.PlaceholderResult;
import net.minecraft.util.Identifier;

class ECPlaceholderRegistry {

    public static void register() {
        var namespace = EssentialCommands.MOD_ID;
        PlaceholderAPI.register(
            new Identifier(namespace, "nickname"),
            (ctx) -> {
                if (ctx.hasPlayer()) {
                    return PlaceholderResult.value(
                        ((ServerPlayerEntityAccess)ctx.getPlayer())
                            .getEcPlayerData().getFullNickname());
                } else {
                    return PlaceholderResult.invalid("No player!");
                }
            }
        );

    }
}
