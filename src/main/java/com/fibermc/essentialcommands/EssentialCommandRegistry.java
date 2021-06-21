package com.fibermc.essentialcommands;

import com.fibermc.essentialcommands.commands.*;
import com.fibermc.essentialcommands.commands.suggestions.HomeSuggestion;
import com.fibermc.essentialcommands.commands.suggestions.TeleportResponseSuggestion;
import com.fibermc.essentialcommands.commands.suggestions.WarpSuggestion;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

import java.util.UUID;

import static net.minecraft.server.command.CommandManager.argument;

/**
 * BasicCommands
 */
public class EssentialCommandRegistry {

    public static void register() {

        CommandRegistrationCallback.EVENT.register(
            (CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) -> {
                final String disabledString = "[EssentialCommands] This command is not enabled.";
                Command<ServerCommandSource> disabledCommandCommand = context -> {
                    context.getSource().getPlayer().sendSystemMessage(
                            new LiteralText(disabledString).setStyle(Config.FORMATTING_ERROR)
                            , new UUID(0,0));
                    return 1;
                };

                //Make some new nodes
                //Tpa
                LiteralArgumentBuilder<ServerCommandSource> tpAskBuilder = CommandManager.literal("tpa");
                LiteralArgumentBuilder<ServerCommandSource> tpAcceptBuilder = CommandManager.literal("tpaccept");
                LiteralArgumentBuilder<ServerCommandSource> tpDenyBuilder = CommandManager.literal("tpdeny");

                if (Config.ENABLE_TPA) {
                    tpAskBuilder.then(
                        argument("target", EntityArgumentType.player())
                            .requires(ECPerms.require("essentialcommands.tpa", 0))
                            .executes(new TeleportAskCommand()));

                    tpAcceptBuilder.then(
                        argument("target", EntityArgumentType.player())
                            .requires(ECPerms.require("essentialcommands.tpaccept", 0))
                            .suggests(TeleportResponseSuggestion.suggestedStrings())
                            .executes(new TeleportAcceptCommand()));

                    tpDenyBuilder.then(
                        argument("target", EntityArgumentType.player())
                            .requires(ECPerms.require("essentialcommands.tpdeny", 0))
                            .suggests(TeleportResponseSuggestion.suggestedStrings())
                            .executes(new TeleportDenyCommand()));
                } else {
                    tpAskBuilder.executes(disabledCommandCommand);
                    tpAcceptBuilder.executes(disabledCommandCommand);
                    tpDenyBuilder.executes(disabledCommandCommand);
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
                            .requires(ECPerms.require("essentialcommands.home.set", 0))
                            .executes(new HomeSetCommand()));

                    homeTpBuilder.then(
                        argument("home_name", StringArgumentType.word())
                            .requires(ECPerms.require("essentialcommands.home.tp", 0))
                            .suggests(HomeSuggestion.suggestedStrings())
                            .executes(new HomeCommand()));

                    homeDeleteBuilder.then(
                        argument("home_name", StringArgumentType.word())
                            .requires(ECPerms.require("essentialcommands.home.delete", 0))
                            .suggests(HomeSuggestion.suggestedStrings())
                            .executes(new HomeDeleteCommand()));

                } else {
                    homeBuilder.executes(disabledCommandCommand);
                    homeSetBuilder.executes(disabledCommandCommand);
                    homeTpBuilder.executes(disabledCommandCommand);
                    homeDeleteBuilder.executes(disabledCommandCommand);
                }


                //Back
                LiteralArgumentBuilder<ServerCommandSource> backBuilder = CommandManager.literal("back");
                if (Config.ENABLE_BACK) {
                    backBuilder
                        .requires(ECPerms.require("essentialcommands.back", 0))
                        .executes(new BackCommand());
                } else {
                    backBuilder.executes(disabledCommandCommand);
                }

                //Warp
                LiteralArgumentBuilder<ServerCommandSource> warpBuilder = CommandManager.literal("warp");
                LiteralArgumentBuilder<ServerCommandSource> warpSetBuilder = CommandManager.literal("set");
                LiteralArgumentBuilder<ServerCommandSource> warpTpBuilder = CommandManager.literal("tp");
                LiteralArgumentBuilder<ServerCommandSource> warpDeleteBuilder = CommandManager.literal("delete");
                if (Config.ENABLE_WARP) {
                    warpSetBuilder.then(
                        argument("warp_name", StringArgumentType.word())
                            .requires(ECPerms.require("essentialcommands.warp.set", 4))
                            .executes(new WarpSetCommand()));

                    warpTpBuilder
                        .then(argument("warp_name", StringArgumentType.word())
                            .requires(ECPerms.require("essentialcommands.warp.tp", 0))
                            .suggests(WarpSuggestion.suggestedStrings())
                            .executes(new WarpTpCommand()));

                    warpDeleteBuilder
                        .then(argument("warp_name", StringArgumentType.word())
                            .requires(ECPerms.require("essentialcommands.warp.delete", 4))
                            .suggests(WarpSuggestion.suggestedStrings())
                            .executes(new WarpDeleteCommand()));

                } else {
                    warpBuilder.executes(disabledCommandCommand);
                    warpSetBuilder.executes(disabledCommandCommand);
                    warpTpBuilder.executes(disabledCommandCommand);
                    warpDeleteBuilder.executes(disabledCommandCommand);
                }

                //Spawn
                LiteralArgumentBuilder<ServerCommandSource> spawnBuilder = CommandManager.literal("spawn");
                LiteralArgumentBuilder<ServerCommandSource> spawnSetBuilder = CommandManager.literal("set");
                LiteralArgumentBuilder<ServerCommandSource> spawnTpBuilder = CommandManager.literal("tp");
                if (Config.ENABLE_SPAWN) {
                    spawnSetBuilder
                            .requires(ECPerms.require("essentialcommands.spawn.set", 4))
                            .executes(new SpawnSetCommand());

                    SpawnCommand cmd = new SpawnCommand();
                    spawnBuilder
                            .requires(ECPerms.require("essentialcommands.spawn.tp", 0))
                            .executes(cmd);
                    spawnTpBuilder
                        .requires(ECPerms.require("essentialcommands.spawn.tp", 0))
                        .executes(cmd);

                } else {
                    spawnBuilder.executes(disabledCommandCommand);
                    spawnTpBuilder.executes(disabledCommandCommand);
                    spawnSetBuilder.executes(disabledCommandCommand);

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

                LiteralCommandNode<ServerCommandSource> essentialCommandsRootNode =
                    CommandManager.literal("essentialcommands").build();
                essentialCommandsRootNode.addChild(spawnNode);
                essentialCommandsRootNode.addChild(warpNode);
                essentialCommandsRootNode.addChild(tpAskNode);
                essentialCommandsRootNode.addChild(tpAcceptNode);
                essentialCommandsRootNode.addChild(tpDenyNode);
                essentialCommandsRootNode.addChild(homeNode);
                essentialCommandsRootNode.addChild(backNode);

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
                        ECPerms.require("essentialcommands.config.reload", 4)
                    ).build();
                configNode.addChild(configReloadNode);
                essentialCommandsRootNode.addChild(configNode);
                rootNode.addChild(essentialCommandsRootNode);
                //TODO if commands are disbleed, don't register them at all
                // @body this allows users to modify EssentialCommands functionality
                // @body so that it does` not conflict with their other mods/config.
            }
        );
    }

}