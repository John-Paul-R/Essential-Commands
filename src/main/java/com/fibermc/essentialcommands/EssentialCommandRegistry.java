package com.fibermc.essentialcommands;

import com.fibermc.essentialcommands.commands.*;
import com.fibermc.essentialcommands.commands.suggestions.HomeSuggestion;
import com.fibermc.essentialcommands.commands.suggestions.TeleportResponseSuggestion;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import me.lucko.fabric.api.permissions.v0.Permissions;
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

    public static void register(ManagerLocator managers) {
        PlayerDataManager dataManager = managers.getPlayerDataManager();
        TeleportRequestManager tpManager = managers.getTpManager();
        WorldDataManager worldDataManager = managers.getWorldDataManager();
        CommandRegistrationCallback.EVENT.register(
            (CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) -> {
                final String disabledString = "[EssentialCommands] This command is not enabled.";
                Command<ServerCommandSource> disabledCommandCommand = context -> {
                    context.getSource().getPlayer().sendSystemMessage(
                            new LiteralText(disabledString).formatted(Config.FORMATTING_ERROR)
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
                            .requires(ECPerms.require("essentialcommands.tpa.ask", 0))
                            .executes(new TeleportAskCommand(tpManager)));

                    tpAcceptBuilder.then(
                        argument("target", EntityArgumentType.player())
                            .requires(ECPerms.require("essentialcommands.tpa.accept", 0))
                            .suggests(TeleportResponseSuggestion.suggestedStrings(dataManager))
                            .executes(new TeleportAcceptCommand(dataManager)));

                    tpDenyBuilder.then(
                        argument("target", EntityArgumentType.player())
                            .requires(ECPerms.require("essentialcommands.tpa.deny", 0))
                            .suggests(TeleportResponseSuggestion.suggestedStrings(dataManager))
                            .executes(new TeleportDenyCommand(dataManager)));
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
                            .executes(new HomeSetCommand(dataManager)));

                    homeTpBuilder.then(
                        argument("home_name", StringArgumentType.word())
                            .requires(ECPerms.require("essentialcommands.home.tp", 0))
                            .suggests(HomeSuggestion.suggestedStrings(dataManager))
                            .executes(new HomeCommand(dataManager)));

                    homeDeleteBuilder.then(
                        argument("home_name", StringArgumentType.word())
                            .requires(ECPerms.require("essentialcommands.home.delete", 0))
                            .suggests(HomeSuggestion.suggestedStrings(dataManager))
                            .executes(new HomeDeleteCommand(dataManager)));

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
                        .executes(new BackCommand(dataManager));
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
                            .executes(new WarpSetCommand(worldDataManager)));

                    warpTpBuilder
                        .then(argument("warp_name", StringArgumentType.word())
                            .requires(ECPerms.require("essentialcommands.warp.tp", 0))
                            .suggests(worldDataManager.getWarpSuggestions())
                            .executes(new WarpTpCommand(dataManager, worldDataManager)));

                    warpDeleteBuilder
                        .then(argument("warp_name", StringArgumentType.word())
                            .requires(ECPerms.require("essentialcommands.warp.delete", 4))
                            .suggests(worldDataManager.getWarpSuggestions())
                            .executes(new WarpDeleteCommand(worldDataManager)));

                } else {
                    warpBuilder.executes(disabledCommandCommand);
                    warpSetBuilder.executes(disabledCommandCommand);
                    warpTpBuilder.executes(disabledCommandCommand);
                    warpDeleteBuilder.executes(disabledCommandCommand);
                }


                    //-=-=-=-=-=-=-=-
                dispatcher.getRoot().addChild(tpAskBuilder.build());
                dispatcher.getRoot().addChild(tpAcceptBuilder.build());
                dispatcher.getRoot().addChild(tpDenyBuilder.build());

                LiteralCommandNode<ServerCommandSource> homeNode = homeBuilder.build();
                dispatcher.getRoot().addChild(homeNode);
                homeNode.addChild(homeTpBuilder.build());
                homeNode.addChild(homeSetBuilder.build());
                homeNode.addChild(homeDeleteBuilder.build());

                dispatcher.getRoot().addChild(backBuilder.build());

                LiteralCommandNode<ServerCommandSource> warpNode = warpBuilder.build();
                dispatcher.getRoot().addChild(warpNode);
                warpNode.addChild(warpTpBuilder.build());
                warpNode.addChild(warpSetBuilder.build());
                warpNode.addChild(warpDeleteBuilder.build());
            }
        );
    }

}