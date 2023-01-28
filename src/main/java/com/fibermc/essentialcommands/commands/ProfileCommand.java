package com.fibermc.essentialcommands.commands;

import java.util.Map;

import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.playerdata.PlayerProfile;
import com.fibermc.essentialcommands.types.ProfileOption;

import com.mojang.brigadier.tree.LiteralCommandNode;

import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;
import static net.minecraft.server.command.CommandManager.argument;

public final class ProfileCommand {
    private ProfileCommand() {}

    public static LiteralCommandNode<ServerCommandSource> buildNode() {
        var root = CommandManager.literal("profile");
        var set = CommandManager.literal("set");
        var get = CommandManager.literal("get");

        for (Map.Entry<String, ProfileOption<?>> entry : PlayerProfile.OPTIONS.entrySet()) {
            var name = entry.getKey();
            var option = entry.getValue();

            set.then(CommandManager.literal(name)
                .then(argument("value", option.argumentType()).executes((context) -> {
                    var player = context.getSource().getPlayerOrThrow();
                    var profile = ((ServerPlayerEntityAccess) player).ec$getProfile();
                    option.profileSetter().setValue(context, "value", profile);
                    profile.markDirty();
                    profile.save();
                    return 0;
                }))
            );

            get.then(CommandManager.literal(name)
                .executes((context) -> {
                    var player = context.getSource().getPlayerOrThrow();
                    var profile = ((ServerPlayerEntityAccess) player).ec$getProfile();
                    context.getSource().sendFeedback(
                        Text.literal(option.profileGetter().getValue(profile).toString()),
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
