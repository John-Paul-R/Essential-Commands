package com.fibermc.essentialcommands;

import com.fibermc.essentialcommands.commands.*;
import com.fibermc.essentialcommands.commands.suggestions.HomeSuggestion;
import com.fibermc.essentialcommands.commands.suggestions.NicknamePlayersSuggestion;
import com.fibermc.essentialcommands.commands.suggestions.TeleportResponseSuggestion;
import com.fibermc.essentialcommands.commands.suggestions.WarpSuggestion;
import com.fibermc.essentialcommands.config.Config;
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

import java.util.function.Predicate;

import static net.minecraft.server.command.CommandManager.argument;

/**
 * BasicCommands
 */
public class EssentialCommandRegistry {

    public static void register() {

        CommandRegistrationCallback.EVENT.register(
            (CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) -> {

                //TODO Command literals still get registered, they just don't do anything if disabled. Fix this.
                RootCommandNode<ServerCommandSource> rootNode = dispatcher.getRoot();

                LiteralCommandNode<ServerCommandSource> essentialCommandsRootNode =
                    CommandManager.literal("essentialcommands").build();


                //Make some new nodes
                //Tpa
                if (Config.ENABLE_TPA) {
                    LiteralArgumentBuilder<ServerCommandSource> tpAskBuilder = CommandManager.literal("tpa");
                    LiteralArgumentBuilder<ServerCommandSource> tpAcceptBuilder = CommandManager.literal("tpaccept");
                    LiteralArgumentBuilder<ServerCommandSource> tpDenyBuilder = CommandManager.literal("tpdeny");

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

                    LiteralCommandNode<ServerCommandSource> tpAskNode = tpAskBuilder.build();
                    LiteralCommandNode<ServerCommandSource> tpAcceptNode = tpAcceptBuilder.build();
                    LiteralCommandNode<ServerCommandSource> tpDenyNode = tpDenyBuilder.build();

                    rootNode.addChild(tpAskNode);
                    rootNode.addChild(tpAcceptNode);
                    rootNode.addChild(tpDenyNode);
                    essentialCommandsRootNode.addChild(tpAskNode);
                    essentialCommandsRootNode.addChild(tpAcceptNode);
                    essentialCommandsRootNode.addChild(tpDenyNode);

                }


                //Homes
                if (Config.ENABLE_HOME) {
                    LiteralArgumentBuilder<ServerCommandSource> homeBuilder = CommandManager.literal("home");
                    LiteralArgumentBuilder<ServerCommandSource> homeSetBuilder = CommandManager.literal("set");
                    LiteralArgumentBuilder<ServerCommandSource> homeTpBuilder = CommandManager.literal("tp");
                    LiteralArgumentBuilder<ServerCommandSource> homeDeleteBuilder = CommandManager.literal("delete");
                    LiteralArgumentBuilder<ServerCommandSource> homeListBuilder = CommandManager.literal("list");

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

                    homeListBuilder
                        .requires(ECPerms.require(ECPerms.Registry.home_tp, 0))
                        .executes(ListCommandFactory.create(
                            "Your current homes are: ",
                            "home tp",
                            HomeSuggestion::getSuggestionEntries
                        ));

                    LiteralCommandNode<ServerCommandSource> homeNode = homeBuilder.build();
                    homeNode.addChild(homeTpBuilder.build());
                    homeNode.addChild(homeSetBuilder.build());
                    homeNode.addChild(homeDeleteBuilder.build());
                    homeNode.addChild(homeListBuilder.build());

                    rootNode.addChild(homeNode);
                    essentialCommandsRootNode.addChild(homeNode);
                }


                //Back
                if (Config.ENABLE_BACK) {
                    LiteralArgumentBuilder<ServerCommandSource> backBuilder = CommandManager.literal("back");
                    backBuilder
                        .requires(ECPerms.require(ECPerms.Registry.back, 0))
                        .executes(new BackCommand());

                    LiteralCommandNode<ServerCommandSource> backNode = backBuilder.build();

                    rootNode.addChild(backNode);
                    essentialCommandsRootNode.addChild(backNode);
                }

                //Warp
                if (Config.ENABLE_WARP) {
                    LiteralArgumentBuilder<ServerCommandSource> warpBuilder = CommandManager.literal("warp");
                    LiteralArgumentBuilder<ServerCommandSource> warpSetBuilder = CommandManager.literal("set");
                    LiteralArgumentBuilder<ServerCommandSource> warpTpBuilder = CommandManager.literal("tp");
                    LiteralArgumentBuilder<ServerCommandSource> warpDeleteBuilder = CommandManager.literal("delete");
                    LiteralArgumentBuilder<ServerCommandSource> warpListBuilder = CommandManager.literal("list");

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

                    warpListBuilder
                        .requires(ECPerms.require(ECPerms.Registry.home_tp, 0))
                        .executes(ListCommandFactory.create(
                            "The available server Warps are: ",
                            "warp tp",
                            (context) -> ManagerLocator.INSTANCE.getWorldDataManager().getWarpEntries()
                        ));


                    LiteralCommandNode<ServerCommandSource> warpNode = warpBuilder.build();
                    warpNode.addChild(warpTpBuilder.build());
                    warpNode.addChild(warpSetBuilder.build());
                    warpNode.addChild(warpDeleteBuilder.build());
                    warpNode.addChild(warpListBuilder.build());


                    rootNode.addChild(warpNode);
                    essentialCommandsRootNode.addChild(warpNode);
                }

                //Spawn
                if (Config.ENABLE_SPAWN) {
                    LiteralArgumentBuilder<ServerCommandSource> spawnBuilder = CommandManager.literal("spawn");
                    LiteralArgumentBuilder<ServerCommandSource> spawnSetBuilder = CommandManager.literal("set");
                    LiteralArgumentBuilder<ServerCommandSource> spawnTpBuilder = CommandManager.literal("tp");

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

                    LiteralCommandNode<ServerCommandSource> spawnNode = spawnBuilder.build();
                    spawnNode.addChild(spawnSetBuilder.build());
                    spawnNode.addChild(spawnTpBuilder.build());

                    rootNode.addChild(spawnNode);
                    essentialCommandsRootNode.addChild(spawnNode);
                }

                //Nickname
                if (Config.ENABLE_NICK) {
                    LiteralArgumentBuilder<ServerCommandSource> nickBuilder = CommandManager.literal("nickname");
                    LiteralArgumentBuilder<ServerCommandSource> nickSetBuilder = CommandManager.literal("set");
                    LiteralArgumentBuilder<ServerCommandSource> nickClearBuilder = CommandManager.literal("clear");
                    LiteralArgumentBuilder<ServerCommandSource> nickRevealBuilder = CommandManager.literal("reveal");

                    Predicate<ServerCommandSource> permissionSelf = ECPerms.require(ECPerms.Registry.nickname_self, 2);
                    Predicate<ServerCommandSource> permissionOther = ECPerms.require(ECPerms.Registry.nickname_others, 4);
                    nickSetBuilder.requires(permissionSelf)
                        .then(argument("nickname", TextArgumentType.text())
                            .executes(new NicknameSetCommand())
                        ).then(argument("target", EntityArgumentType.player())
                            .requires(permissionOther)
                            .then(argument("nickname", TextArgumentType.text())
                                .executes(new NicknameSetCommand())
                            ).then(argument("nickname_placeholder_api", StringArgumentType.greedyString())
                                .executes(NicknameSetCommand::runStringToText)
                            )
                        ).then(argument("nickname_placeholder_api", StringArgumentType.greedyString())
                            .executes(NicknameSetCommand::runStringToText)
                        );

                    nickClearBuilder
                        .requires(ECPerms.require(ECPerms.Registry.nickname_self, 2))
                        .executes(new NicknameClearCommand())
                        .then(argument("target", EntityArgumentType.player())
                            .requires(ECPerms.require(ECPerms.Registry.nickname_others, 4))
                            .executes(new NicknameClearCommand()));

                    nickRevealBuilder
                        .requires(ECPerms.require(ECPerms.Registry.nickname_reveal, 4))
                        .then(argument("player_nickname", StringArgumentType.word())
                            .suggests(NicknamePlayersSuggestion.suggestedStrings())
                            .executes(new RealNameCommand())
                        );

                    LiteralCommandNode<ServerCommandSource> nickNode = nickBuilder.build();
                    nickNode.addChild(nickSetBuilder.build());
                    nickNode.addChild(nickClearBuilder.build());
                    nickNode.addChild(nickRevealBuilder.build());

                    rootNode.addChild(nickNode);
                    essentialCommandsRootNode.addChild(nickNode);
                }

                if (Config.ENABLE_RTP) {
                    LiteralCommandNode<ServerCommandSource> rtpNode = dispatcher.register(
                        CommandManager.literal("randomteleport")
                            .requires(ECPerms.require(ECPerms.Registry.randomteleport, 2))
                            .executes(new RandomTeleportCommand())
                    );

                    dispatcher.register(CommandManager.literal("rtp")
                        .requires(ECPerms.require(ECPerms.Registry.randomteleport, 2))
                        .executes(new RandomTeleportCommand())
                    );
                    essentialCommandsRootNode.addChild(rtpNode);

                }

                if (Config.ENABLE_FLY) {
                    LiteralCommandNode<ServerCommandSource> flyNode = dispatcher.register(
                        CommandManager.literal("fly")
                            .requires(ECPerms.require(ECPerms.Registry.fly_self, 2))
                            .executes(new FlyCommand())
                            .then(argument("target_player", EntityArgumentType.player())
                                .requires(ECPerms.require(ECPerms.Registry.fly_others, 4))
                                .executes(new FlyCommand())
                            )
                    );

                    essentialCommandsRootNode.addChild(flyNode);
                }

                if (Config.ENABLE_WORKBENCH) {
                    LiteralCommandNode<ServerCommandSource> workbenchNode = dispatcher.register(
                        CommandManager.literal("workbench")
                            .requires(ECPerms.require(ECPerms.Registry.workbench, 2))
                            .executes(new WorkbenchCommand())
                    );

                    essentialCommandsRootNode.addChild(workbenchNode);
                }

                if (Config.ENABLE_ENDERCHEST) {
                    LiteralCommandNode<ServerCommandSource> enderchestNode = dispatcher.register(
                        CommandManager.literal("enderchest")
                            .requires(ECPerms.require(ECPerms.Registry.enderchest, 2))
                            .executes(new EnderchestCommand())
                    );

                    essentialCommandsRootNode.addChild(enderchestNode);
                }


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