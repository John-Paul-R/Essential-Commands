package com.fibermc.essentialcommands;

import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import eu.pb4.placeholders.PlaceholderAPI;
import eu.pb4.placeholders.PlaceholderResult;

import net.minecraft.util.Identifier;

final class ECPlaceholderRegistry {
    private ECPlaceholderRegistry() {}

    public static void register() {
        var namespace = EssentialCommands.MOD_ID;
        PlaceholderAPI.register(
            new Identifier(namespace, "nickname"),
            (ctx) -> {
                if (ctx.hasPlayer()) {
                    return PlaceholderResult.value(
                        ((ServerPlayerEntityAccess)ctx.getPlayer())
                            .ec$getPlayerData()
                            .getFullNickname());
                }
                return PlaceholderResult.invalid("No player!");
            }
        );

    }
}
