package com.fibermc.essentialcommands;

import com.fibermc.essentialcommands.commands.*;
import com.fibermc.essentialcommands.commands.suggestions.HomeSuggestion;
import com.fibermc.essentialcommands.commands.suggestions.TeleportResponseSuggestion;
import com.fibermc.essentialcommands.commands.suggestions.WarpSuggestion;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.TextArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

import static net.minecraft.server.command.CommandManager.argument;

/**
 * BasicCommands
 */
public class EssentialCommandRegistry {

    public static void register() {

        CommandRegistrationCallback.EVENT.register(
            (CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) -> {
                //Make some new nodes
                //Tpa
                LiteralArgumentBuilder<ServerCommandSource> tpAskBuilder = CommandManager.literal("tpa");
                LiteralArgumentBuilder<ServerCommandSource> tpAcceptBuilder = CommandManager.literal("tpaccept");
                LiteralArgumentBuilder<ServerCommandSource> tpDenyBuilder = CommandManager.literal("tpdeny");

                if (Config.ENABLE_TPA) {
                    tpAskBuilder.then(
                        argument("target", EntityArgumentType.player())
                            .requires(ECPerms.require(ECPerms.Registry.tpa, 0))
                            .executes(new TeleportAskCommand()));

                    tpAcceptBuilder.then(
                        argument("target", EntityArgumentType.player())
                            .requires(ECPerms.require(ECPerms.Registry.tpaccept, 0))
                            .suggests(TeleportResponseSuggestion.suggestedStrings())
                            .executes(new TeleportAcceptCommand()));

                    tpDenyBuilder.then(
                        argument("target", EntityArgumentType.player())
                            .requires(ECPerms.require(ECPerms.Registry.tpdeny, 0))
                            .suggests(TeleportResponseSuggestion.suggestedStrings())
                            .executes(new TeleportDenyCommand()));
                }


                //Homes
                LiteralArgumentBuilder<ServerCommandSource> homeBuilder = CommandManager.literal("home");
                LiteralArgumentBuilder<ServerCommandSource> homeSetBuilder = CommandManager.literal("set");
                LiteralArgumentBuilder<ServerCommandSource> homeTpBuilder = CommandManager.literal("tp");
                LiteralArgumentBuilder<ServerCommandSource> homeDeleteBuilder = CommandManager.literal("delete");
                if (Config.ENABLE_HOME) {
                    //homeBuilder;

                    homeSetBuilder.then(
                        argument("home_name", StringArgumentType.word())
                            .requires(ECPerms.require(ECPerms.Registry.home_set, 0))
                            .executes(new HomeSetCommand()));

                    homeTpBuilder.then(
                        argument("home_name", StringArgumentType.word())
                            .requires(ECPerms.require(ECPerms.Registry.home_tp, 0))
                            .suggests(HomeSuggestion.suggestedStrings())
                            .executes(new HomeCommand()));

                    homeDeleteBuilder.then(
                        argument("home_name", StringArgumentType.word())
                            .requires(ECPerms.require(ECPerms.Registry.home_delete, 0))
                            .suggests(HomeSuggestion.suggestedStrings())
                            .executes(new HomeDeleteCommand()));

                }


                //Back
                LiteralArgumentBuilder<ServerCommandSource> backBuilder = CommandManager.literal("back");
                if (Config.ENABLE_BACK) {
                    backBuilder
                        .requires(ECPerms.require(ECPerms.Registry.back, 0))
                        .executes(new BackCommand());
                }

                //Warp
                LiteralArgumentBuilder<ServerCommandSource> warpBuilder = CommandManager.literal("warp");
                LiteralArgumentBuilder<ServerCommandSource> warpSetBuilder = CommandManager.literal("set");
                LiteralArgumentBuilder<ServerCommandSource> warpTpBuilder = CommandManager.literal("tp");
                LiteralArgumentBuilder<ServerCommandSource> warpDeleteBuilder = CommandManager.literal("delete");
                if (Config.ENABLE_WARP) {
                    warpSetBuilder.then(
                        argument("warp_name", StringArgumentType.word())
                            .requires(ECPerms.require(ECPerms.Registry.warp_set, 4))
                            .executes(new WarpSetCommand()));

                    warpTpBuilder
                        .then(argument("warp_name", StringArgumentType.word())
                            .requires(ECPerms.require(ECPerms.Registry.warp_tp, 0))
                            .suggests(WarpSuggestion.suggestedStrings())
                            .executes(new WarpTpCommand()));

                    warpDeleteBuilder
                        .then(argument("warp_name", StringArgumentType.word())
                            .requires(ECPerms.require(ECPerms.Registry.warp_delete, 4))
                            .suggests(WarpSuggestion.suggestedStrings())
                            .executes(new WarpDeleteCommand()));

                }

                //Spawn
                LiteralArgumentBuilder<ServerCommandSource> spawnBuilder = CommandManager.literal("spawn");
                LiteralArgumentBuilder<ServerCommandSource> spawnSetBuilder = CommandManager.literal("set");
                LiteralArgumentBuilder<ServerCommandSource> spawnTpBuilder = CommandManager.literal("tp");
                if (Config.ENABLE_SPAWN) {
                    spawnSetBuilder
                            .requires(ECPerms.require(ECPerms.Registry.spawn_set, 4))
                            .executes(new SpawnSetCommand());

                    SpawnCommand cmd = new SpawnCommand();
                    spawnBuilder
                            .requires(ECPerms.require(ECPerms.Registry.spawn_tp, 0))
                            .executes(cmd);
                    spawnTpBuilder
                        .requires(ECPerms.require(ECPerms.Registry.spawn_tp, 0))
                        .executes(cmd);

                }
                //Spawn
                LiteralArgumentBuilder<ServerCommandSource> nickBuilder = CommandManager.literal("nickname");
                LiteralArgumentBuilder<ServerCommandSource> nickSetBuilder = CommandManager.literal("set");
                LiteralArgumentBuilder<ServerCommandSource> nickClearBuilder = CommandManager.literal("clear");
                if (Config.ENABLE_NICK) {
                    nickSetBuilder
                        .requires(ECPerms.require(ECPerms.Registry.nickname_set, 2))
                        .then(argument("nickname", TextArgumentType.text())
                            .executes(new NicknameSetCommand())
                        );

                    nickClearBuilder
                        .requires(ECPerms.require(ECPerms.Registry.nickname_set, 2))
                        .executes(new NicknameClearCommand());

                }

                RootCommandNode<ServerCommandSource> rootNode = dispatcher.getRoot();
                    //-=-=-=-=-=-=-=-
                LiteralCommandNode<ServerCommandSource> tpAskNode = tpAskBuilder.build();
                LiteralCommandNode<ServerCommandSource> tpAcceptNode = tpAcceptBuilder.build();
                LiteralCommandNode<ServerCommandSource> tpDenyNode = tpDenyBuilder.build();

                rootNode.addChild(tpAskNode);
                rootNode.addChild(tpAcceptNode);
                rootNode.addChild(tpDenyNode);

                LiteralCommandNode<ServerCommandSource> homeNode = homeBuilder.build();
                rootNode.addChild(homeNode);
                homeNode.addChild(homeTpBuilder.build());
                homeNode.addChild(homeSetBuilder.build());
                homeNode.addChild(homeDeleteBuilder.build());

                LiteralCommandNode<ServerCommandSource> backNode = backBuilder.build();
                rootNode.addChild(backNode);

                LiteralCommandNode<ServerCommandSource> warpNode = warpBuilder.build();
                rootNode.addChild(warpNode);
                warpNode.addChild(warpTpBuilder.build());
                warpNode.addChild(warpSetBuilder.build());
                warpNode.addChild(warpDeleteBuilder.build());

                LiteralCommandNode<ServerCommandSource> spawnNode = spawnBuilder.build();
                rootNode.addChild(spawnNode);
                spawnNode.addChild(spawnSetBuilder.build());
                spawnNode.addChild(spawnTpBuilder.build());

                LiteralCommandNode<ServerCommandSource> nickNode = nickBuilder.build();
                rootNode.addChild(nickNode);
                nickNode.addChild(nickSetBuilder.build());
                nickNode.addChild(nickClearBuilder.build());

                LiteralCommandNode<ServerCommandSource> essentialCommandsRootNode =
                    CommandManager.literal("essentialcommands").build();
                essentialCommandsRootNode.addChild(spawnNode);
                essentialCommandsRootNode.addChild(warpNode);
                essentialCommandsRootNode.addChild(tpAskNode);
                essentialCommandsRootNode.addChild(tpAcceptNode);
                essentialCommandsRootNode.addChild(tpDenyNode);
                essentialCommandsRootNode.addChild(homeNode);
                essentialCommandsRootNode.addChild(backNode);
                essentialCommandsRootNode.addChild(nickNode);

                LiteralCommandNode<ServerCommandSource> configNode = CommandManager.literal("config").build();

                LiteralCommandNode<ServerCommandSource> configReloadNode = CommandManager.literal("reload")
                    .executes((context) -> {
                        Config.loadOrCreateProperties();
                        context.getSource().sendFeedback(
                            new LiteralText("[Essential Commands] Config Reloaded."),
                            true
                        );
                        return 1;
                    }).requires(
                        ECPerms.require(ECPerms.Registry.config_reload, 4)
                    ).build();
                configNode.addChild(configReloadNode);
                essentialCommandsRootNode.addChild(configNode);
                rootNode.addChild(essentialCommandsRootNode);
            }
        );
    }

}