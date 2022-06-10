package com.fibermc.essentialcommands;

import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
import net.minecraft.util.Identifier;

class ECPlaceholderRegistry {

    public static void register() {
        var namespace = EssentialCommands.MOD_ID;
        Placeholders.register(
            new Identifier(namespace, "nickname"),
            (ctx, arg) -> {
                if (ctx.hasPlayer()) {
                    return PlaceholderResult.value(
                        ((ServerPlayerEntityAccess)ctx.player())
                            .getEcPlayerData()
                            .getFullNickname());
                }
                return PlaceholderResult.invalid("No player!");
            }
        );

    }
}
