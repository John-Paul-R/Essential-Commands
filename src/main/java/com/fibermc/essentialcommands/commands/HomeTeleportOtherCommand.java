package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.PlayerData;
import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.commands.suggestions.ListSuggestion;
import com.fibermc.essentialcommands.types.MinecraftLocation;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class HomeTeleportOtherCommand extends HomeCommand implements Command<ServerCommandSource> {

    public HomeTeleportOtherCommand() {
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        PlayerData senderPlayerData = ((ServerPlayerEntityAccess) context.getSource().getPlayer()).getEcPlayerData();
        String homeName = StringArgumentType.getString(context, "home_name");

        return HomeCommand.exec(senderPlayerData, getTargetPlayerData(context), homeName);
    }

    private static PlayerData getTargetPlayerData(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return ((ServerPlayerEntityAccess) EntityArgumentType.getPlayer(context, "target_player")).getEcPlayerData();
    }

    public int runDefault(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var senderPlayerData = ((ServerPlayerEntityAccess) context.getSource().getPlayer()).getEcPlayerData();
        var targetPlayerData = getTargetPlayerData(context);

        return HomeCommand.exec(
            senderPlayerData,
            targetPlayerData,
            HomeCommand.getSoleHomeName(targetPlayerData)
        );
    }

    public static class Suggestion {
        //Brigader Suggestions
        public static final SuggestionProvider<ServerCommandSource> suggestedStrings
            = ListSuggestion.ofContext(HomeTeleportOtherCommand.Suggestion::getSuggestionsList);

        /**
         * Gets a list of suggested strings to be used with Brigader
         */
        public static List<String> getSuggestionsList(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
            return new ArrayList<>(HomeTeleportOtherCommand.getTargetPlayerData(context).getHomeNames());
        }

        /**
         * Gets a set of suggestion entries to be used with ListCommandFactory
         */
        public static Set<Map.Entry<String, MinecraftLocation>> getSuggestionEntries(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
            return HomeTeleportOtherCommand.getTargetPlayerData(context).getHomeEntries();
        }

    }
}
