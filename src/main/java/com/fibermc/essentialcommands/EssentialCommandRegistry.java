package com.fibermc.essentialcommands;

import com.fibermc.essentialcommands.commands.*;
import com.fibermc.essentialcommands.commands.suggestions.*;
import com.fibermc.essentialcommands.config.Config;
import com.fibermc.essentialcommands.util.EssentialsXParser;
import com.fibermc.essentialcommands.util.TextUtil;
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

import java.io.FileNotFoundException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
                    LiteralCommandNode<ServerCommandSource> tpAskNode = CommandManager.literal("tpa")
                        .requires(ECPerms.require(ECPerms.Registry.tpa, 0))
                        .then(argument("target", EntityArgumentType.player())
                            .executes(new TeleportAskCommand())
                        ).build();

                    LiteralCommandNode<ServerCommandSource> tpAcceptNode = CommandManager.literal("tpaccept")
                        .requires(ECPerms.require(ECPerms.Registry.tpaccept, 0))
                        .executes(new TeleportAcceptCommand()::runDefault)
                        .then(argument("target", EntityArgumentType.player())
                            .suggests(TeleportResponseSuggestion.suggestedStrings())
                            .executes(new TeleportAcceptCommand())
                        ).build();

                    LiteralCommandNode<ServerCommandSource> tpDenyNode = CommandManager.literal("tpdeny")
                        .requires(ECPerms.require(ECPerms.Registry.tpdeny, 0))
                        .executes(new TeleportDenyCommand()::runDefault)
                        .then(argument("target", EntityArgumentType.player())
                            .suggests(TeleportResponseSuggestion.suggestedStrings())
                            .executes(new TeleportDenyCommand())
                        ).build();

                    LiteralCommandNode<ServerCommandSource> tpAskHereNode = CommandManager.literal("tpahere")
                            .requires(ECPerms.require(ECPerms.Registry.tpahere, 0))
                            .then(argument("target", EntityArgumentType.player())
                                    .executes(new TeleportAskHereCommand())
                            ).build();

                    rootNode.addChild(tpAskNode);
                    rootNode.addChild(tpAcceptNode);
                    rootNode.addChild(tpDenyNode);
                    rootNode.addChild(tpAskHereNode);

                    essentialCommandsRootNode.addChild(tpAskNode);
                    essentialCommandsRootNode.addChild(tpAcceptNode);
                    essentialCommandsRootNode.addChild(tpDenyNode);
                    essentialCommandsRootNode.addChild(tpAskHereNode);


                }


                //Homes
                if (Config.ENABLE_HOME) {
                    LiteralArgumentBuilder<ServerCommandSource> homeBuilder = CommandManager.literal("home");
                    LiteralArgumentBuilder<ServerCommandSource> homeSetBuilder = CommandManager.literal("set");
                    LiteralArgumentBuilder<ServerCommandSource> homeTpBuilder = CommandManager.literal("tp");
                    LiteralArgumentBuilder<ServerCommandSource> homeDeleteBuilder = CommandManager.literal("delete");
                    LiteralArgumentBuilder<ServerCommandSource> homeListBuilder = CommandManager.literal("list");

                    homeSetBuilder
                        .requires(ECPerms.require(ECPerms.Registry.home_set, 0))
                        .then(argument("home_name", StringArgumentType.word())
                            .executes(new HomeSetCommand()));

                    homeTpBuilder
                        .requires(ECPerms.require(ECPerms.Registry.home_tp, 0))
                        .executes(new HomeCommand()::runDefault)
                        .then(argument("home_name", StringArgumentType.word())
                            .suggests(HomeSuggestion.suggestedStrings())
                            .executes(new HomeCommand()));

                    homeDeleteBuilder
                        .requires(ECPerms.require(ECPerms.Registry.home_delete, 0))
                        .then(argument("home_name", StringArgumentType.word())
                            .suggests(HomeSuggestion.suggestedStrings())
                            .executes(new HomeDeleteCommand()));

                    homeListBuilder
                        .requires(ECPerms.require(ECPerms.Registry.home_tp, 0))
                        .executes(ListCommandFactory.create(
                            ECText.getInstance().get("cmd.home.list.start"),
                            "home tp",
                            HomeSuggestion::getSuggestionEntries
                        ));

                    LiteralCommandNode<ServerCommandSource> homeNode = homeBuilder
                            .requires(ECPerms.requireAny(ECPerms.Registry.Group.home_group, 0))
                            .build();
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

                    warpSetBuilder
                        .requires(ECPerms.require(ECPerms.Registry.warp_set, 4))
                        .then(argument("warp_name", StringArgumentType.word())
                            .executes(new WarpSetCommand()));

                    warpTpBuilder
                        .requires(ECPerms.require(ECPerms.Registry.warp_tp, 0))
                        .then(argument("warp_name", StringArgumentType.word())
                            .suggests(WarpSuggestion.suggestedStrings())
                            .executes(new WarpTpCommand()));

                    warpDeleteBuilder
                        .requires(ECPerms.require(ECPerms.Registry.warp_delete, 4))
                        .then(argument("warp_name", StringArgumentType.word())
                            .suggests(WarpSuggestion.suggestedStrings())
                            .executes(new WarpDeleteCommand()));

                    warpListBuilder
                        .requires(ECPerms.require(ECPerms.Registry.warp_tp, 0))
                        .executes(ListCommandFactory.create(
                            ECText.getInstance().get("cmd.warp.list.start"),
                            "warp tp",
                            (context) -> ManagerLocator.INSTANCE.getWorldDataManager().getWarpEntries()
                        ));


                    LiteralCommandNode<ServerCommandSource> warpNode = warpBuilder
                            .requires(ECPerms.requireAny(ECPerms.Registry.Group.warp_group, 0))
                            .build();
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

                    LiteralCommandNode<ServerCommandSource> nickNode = nickBuilder
                            .requires(ECPerms.requireAny(ECPerms.Registry.Group.nickname_group, 2))
                            .build();
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
                            .requires(ECPerms.require(ECPerms.Registry.workbench, 0))
                            .executes(new WorkbenchCommand())
                    );

                    essentialCommandsRootNode.addChild(workbenchNode);
                }

                if (Config.ENABLE_ENDERCHEST) {
                    LiteralCommandNode<ServerCommandSource> enderchestNode = dispatcher.register(
                        CommandManager.literal("enderchest")
                            .requires(ECPerms.require(ECPerms.Registry.enderchest, 0))
                            .executes(new EnderchestCommand())
                    );

                    essentialCommandsRootNode.addChild(enderchestNode);
                }

                if (Config.ENABLE_TOP) {
                    LiteralCommandNode<ServerCommandSource> topNode = dispatcher.register(
                        CommandManager.literal("top")
                            .requires(ECPerms.require(ECPerms.Registry.top, 2))
                            .executes(new TopCommand())
                    );

                    essentialCommandsRootNode.addChild(topNode);
                }


                LiteralCommandNode<ServerCommandSource> configNode = CommandManager.literal("config")
                        .requires(ECPerms.requireAny(ECPerms.Registry.Group.config_group, 4))
                        .then(CommandManager.literal("reload")
                            .executes((context) -> {
                                Config.loadOrCreateProperties();
                                context.getSource().sendFeedback(
                                    TextUtil.concat(
                                        ECText.getInstance().getText("essentialcommands.fullprefix"),
                                        ECText.getInstance().getText("cmd.config.reload")
                                    ),
                                true
                                );
                                return 1;
                            }).requires(
                                    ECPerms.require(ECPerms.Registry.config_reload, 4)
                            ).build())
                        .then(CommandManager.literal("display")
                            .requires(ECPerms.require(ECPerms.Registry.config_reload, 4))
                            .executes((context) -> {
                                Config.loadOrCreateProperties();
                                context.getSource().sendFeedback(
                                    Config.stateAsText(),
                                    false
                                );
                                return 1;
                            })
                            .then(CommandManager.argument("config_property", StringArgumentType.word())
                                .suggests(ListSuggestion.of(Config::getPubFieldNames))
                                .executes(context -> {
                                    try {
                                        context.getSource().sendFeedback(Config.getFieldValueAsText(
                                                StringArgumentType.getString(context, "config_property")
                                        ), false);
                                    } catch (NoSuchFieldException e) {
                                        e.printStackTrace();
                                    }

                                    return 1;
                                })
                            )
                        ).build();

                essentialCommandsRootNode.addChild(configNode);

                if (Config.ENABLE_ESSENTIALSX_CONVERT) {
                    essentialCommandsRootNode.addChild(CommandManager.literal("convertEssentialsXPlayerHomes")
                        .requires(source -> source.hasPermissionLevel(4))
                        .executes((source) -> {
                            Path mcDir = source.getSource().getMinecraftServer().getRunDirectory().toPath();
                            try {
                                EssentialsXParser.convertPlayerDataDir(
                                        mcDir.resolve("plugins/Essentials/userdata").toFile(),
                                        mcDir.resolve("world/modplayerdata").toFile(),
                                        source.getSource().getMinecraftServer()
                                );
                                source.getSource().sendFeedback(new LiteralText("Successfully converted data dirs."), Config.BROADCAST_TO_OPS);
                            } catch (NotDirectoryException | FileNotFoundException e) {
                                e.printStackTrace();
                            }
                            return 0;
                        }).build()
                    );
                }

//                essentialCommandsRootNode.addChild(CommandManager.literal("test")
//                        .executes(configReloadNode.getCommand())
//                        .build()
//                );

                rootNode.addChild(essentialCommandsRootNode);
            }
        );
    }

}