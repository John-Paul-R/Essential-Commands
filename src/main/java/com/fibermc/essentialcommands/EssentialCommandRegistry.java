package com.fibermc.essentialcommands;

import com.fibermc.essentialcommands.commands.*;
import com.fibermc.essentialcommands.commands.suggestions.HomeSuggestion;
import com.fibermc.essentialcommands.commands.suggestions.TeleportResponseSuggestion;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.registry.CommandRegistry;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.network.MessageType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

import static net.minecraft.server.command.CommandManager.argument;

/**
 * BasicCommands
 */
public class EssentialCommandRegistry {

    public static void register(ManagerLocator managers) {
        PlayerDataManager dataManager = managers.getDataManager();
        TeleportRequestManager tpManager = managers.getTpManager();

        CommandRegistry.INSTANCE.register(false, dispatcher -> {
            final String disabledString = "[EssentialCommands] This command is not enabled.";
            Command<ServerCommandSource> disabledCommandCommand = context -> {
                context.getSource().getPlayer().sendChatMessage(
                        new LiteralText(disabledString).formatted(Config.FORMATTING_ERROR)
                        , MessageType.SYSTEM);
                return 1;
            };

            //Make some new nodes
            //Tpa
            LiteralArgumentBuilder<ServerCommandSource> tpAskBuilder = CommandManager.literal("tpa");
            LiteralArgumentBuilder<ServerCommandSource> tpAcceptBuilder = CommandManager.literal("tpaccept");
            LiteralArgumentBuilder<ServerCommandSource> tpDenyBuilder = CommandManager.literal("tpdeny");

            if (Config.ENABLE_TPA) {
                 tpAskBuilder
                         .then(
                                 argument("target", EntityArgumentType.player())
                                 .executes(new TeleportAskCommand(tpManager)))
                         .build();

                tpAcceptBuilder
                        .then(
                                argument("target", EntityArgumentType.player())
                                .suggests(TeleportResponseSuggestion.suggestedStrings(dataManager))
                                .executes(new TeleportAcceptCommand(dataManager)))
                        .build();

                tpDenyBuilder
                        .then(
                                argument("target", EntityArgumentType.player())
                                        .suggests(TeleportResponseSuggestion.suggestedStrings(dataManager))
                                        .executes(new TeleportDenyCommand(dataManager)))
                        .build();
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
                                .executes(new HomeSetCommand(dataManager)));

                homeTpBuilder
                        .then(argument("home_name", StringArgumentType.word())
                                .suggests(HomeSuggestion.suggestedStrings(dataManager))
                                .executes(new HomeCommand(dataManager)));

                homeDeleteBuilder
                        .then(argument("home_name", StringArgumentType.word())
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
                backBuilder.executes(new BackCommand(dataManager));
            } else {
                backBuilder.executes(disabledCommandCommand);
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
        });
    }

}