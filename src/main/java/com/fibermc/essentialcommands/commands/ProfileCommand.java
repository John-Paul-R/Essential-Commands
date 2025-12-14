package com.fibermc.essentialcommands.commands;

import java.util.Map;

import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.playerdata.PlayerProfile;
import com.fibermc.essentialcommands.types.ProfileOption;

import com.mojang.brigadier.tree.LiteralCommandNode;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;
import static net.minecraft.commands.Commands.argument;

public final class ProfileCommand {
    private ProfileCommand() {}

    public static LiteralCommandNode<CommandSourceStack> buildNode() {
        var root = Commands.literal("profile");
        var set = Commands.literal("set");
        var get = Commands.literal("get");

        for (Map.Entry<String, ProfileOption<?>> entry : PlayerProfile.OPTIONS.entrySet()) {
            var name = entry.getKey();
            var option = entry.getValue();

            set.then(Commands.literal(name)
                .then(argument("value", option.argumentType()).executes((context) -> {
                    var player = context.getSource().getPlayerOrException();
                    var profile = ((ServerPlayerEntityAccess) player).ec$getProfile();
                    option.profileSetter().setValue(context, "value", profile);
                    profile.setDirty();
                    profile.save(context.getSource().getServer().registryAccess());
                    return 0;
                }))
            );

            get.then(Commands.literal(name)
                .executes((context) -> {
                    var player = context.getSource().getPlayerOrException();
                    var profile = ((ServerPlayerEntityAccess) player).ec$getProfile();
                    context.getSource().sendSuccess(() ->
                        Component.literal(option.profileGetter().getValue(profile).map(Object::toString).orElse("<not set>")),
                        CONFIG.BROADCAST_TO_OPS);
                    return 0;
                })
            );
        }

        root.then(set);
        root.then(get);
        return root.build();
    }
}
