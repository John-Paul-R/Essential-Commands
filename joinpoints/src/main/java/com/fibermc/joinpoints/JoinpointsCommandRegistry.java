package com.fibermc.joinpoints;

import com.fibermc.joinpoints.commands.*;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.server.command.CommandManager.argument;

public final class JoinpointsCommandRegistry {
    private JoinpointsCommandRegistry() {}

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            if (!Joinpoints.getConfig().ENABLE_JOINPOINT.getValue()) {
                return;
            }

            var joinpointBuilder = CommandManager.literal("joinpoint");
            var joinpointSetBuilder = CommandManager.literal("set");
            var joinpointTpBuilder = CommandManager.literal("tp");
            var joinpointDeleteBuilder = CommandManager.literal("delete");
            var joinpointOverwriteBuilder = CommandManager.literal("overwrite");
            var joinpointShareBuilder = CommandManager.literal("share");
            var joinpointListBuilder = CommandManager.literal("list");

            joinpointSetBuilder
                .requires(JoinpointsPerms.require(JoinpointsPerms.Registry.joinpoint_set, 0))
                .then(argument("joinpoint_name", StringArgumentType.word())
                    .executes(new JoinpointSetCommand(JoinpointSetCommand.Action.SET))
                    .then(argument("global", BoolArgumentType.bool())
                        .executes(new JoinpointSetCommand(JoinpointSetCommand.Action.SET))));

            joinpointTpBuilder
                .requires(JoinpointsPerms.require(JoinpointsPerms.Registry.joinpoint_tp, 0))
                .then(argument(JoinpointTpCommand.OWNER_PLAYER_ARG, StringArgumentType.word())
                    .suggests(JoinpointTpCommand.Suggestion.OWNERS_OF_ACCESSIBLE_JOINPOINTS)
                    .then(argument("joinpoint_name", StringArgumentType.word())
                        .suggests(JoinpointTpCommand.Suggestion.ACCESSIBLE_TARGET_PLAYER_JOINPOINTS)
                        .executes(new JoinpointTpCommand())));

            joinpointDeleteBuilder
                .requires(JoinpointsPerms.require(JoinpointsPerms.Registry.joinpoint_delete, 0))
                .then(argument("joinpoint_name", StringArgumentType.word())
                    .suggests(JoinpointTpCommand.Suggestion.OWNED_JOINPOINTS)
                    .executes(new JoinpointSetCommand(JoinpointSetCommand.Action.DELETE)));

            joinpointOverwriteBuilder
                .requires(JoinpointsPerms.require(JoinpointsPerms.Registry.joinpoint_set, 0))
                .then(argument("joinpoint_name", StringArgumentType.word())
                    .suggests(JoinpointTpCommand.Suggestion.OWNED_JOINPOINTS)
                    .executes(new JoinpointSetCommand(JoinpointSetCommand.Action.OVERWRITE))
                    .then(argument("global", BoolArgumentType.bool())
                        .executes(new JoinpointSetCommand(JoinpointSetCommand.Action.OVERWRITE))));

            joinpointShareBuilder
                .requires(JoinpointsPerms.require(JoinpointsPerms.Registry.joinpoint_set, 0))
                .then(argument("joinpoint_name", StringArgumentType.word())
                    .suggests(JoinpointTpCommand.Suggestion.OWNED_JOINPOINTS)
                    .then(CommandManager.literal("add")
                        .then(argument("target_players", EntityArgumentType.players())
                            .executes(new JoinpointShareCommand(JoinpointShareCommand.Action.ADD))))
                    .then(CommandManager.literal("remove")
                        .then(argument("target_players", EntityArgumentType.players())
                            .executes(new JoinpointShareCommand(JoinpointShareCommand.Action.REMOVE))))
                    .then(CommandManager.literal("list")
                        .executes(new JoinpointShareCommand(JoinpointShareCommand.Action.LIST)))
                    .then(CommandManager.literal("clear")
                        .executes(new JoinpointShareCommand(JoinpointShareCommand.Action.CLEAR))));

            joinpointListBuilder
                .requires(JoinpointsPerms.require(JoinpointsPerms.Registry.joinpoint_tp, 0))
                .executes(new JoinpointListCommand()::runDefault)
                .then(argument("filter", StringArgumentType.word())
                    .suggests(JoinpointListCommand.Suggestion.FILTER_TYPES)
                    .executes(new JoinpointListCommand()));

            LiteralCommandNode<ServerCommandSource> joinpointNode = joinpointBuilder
                .requires(JoinpointsPerms.requireAny(JoinpointsPerms.Registry.Group.joinpoint_group, 0))
                .build();
            joinpointNode.addChild(joinpointSetBuilder.build());
            joinpointNode.addChild(joinpointTpBuilder.build());
            joinpointNode.addChild(joinpointDeleteBuilder.build());
            joinpointNode.addChild(joinpointOverwriteBuilder.build());
            joinpointNode.addChild(joinpointShareBuilder.build());
            joinpointNode.addChild(joinpointListBuilder.build());

            dispatcher.getRoot().addChild(joinpointNode);
        });
    }
}
