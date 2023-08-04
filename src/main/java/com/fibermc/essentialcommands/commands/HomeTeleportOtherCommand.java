package com.fibermc.essentialcommands.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.fibermc.essentialcommands.ManagerLocator;
import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.commands.suggestions.ListSuggestion;
import com.fibermc.essentialcommands.playerdata.PlayerData;
import com.fibermc.essentialcommands.playerdata.PlayerProfile;
import com.fibermc.essentialcommands.text.ECText;
import com.fibermc.essentialcommands.types.NamedMinecraftLocation;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;

public class HomeTeleportOtherCommand extends HomeCommand implements Command<ServerCommandSource> {
    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        PlayerData senderPlayerData = ((ServerPlayerEntityAccess) context.getSource().getPlayerOrThrow()).ec$getPlayerData();
        String homeName = StringArgumentType.getString(context, "home_name");

        return HomeCommand.exec(senderPlayerData, getTargetPlayerData(context), homeName);
    }

    private static PlayerData getTargetPlayerData(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return ((ServerPlayerEntityAccess) EntityArgumentType.getPlayer(context, "target_player")).ec$getPlayerData();
    }

    public int runDefault(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var senderPlayerData = ((ServerPlayerEntityAccess) context.getSource().getPlayerOrThrow()).ec$getPlayerData();
        var targetPlayerData = getTargetPlayerData(context);

        return HomeCommand.exec(
            senderPlayerData,
            targetPlayerData,
            HomeCommand.getSoleHomeName(targetPlayerData)
        );
    }

    public int runOfflinePlayer(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        PlayerData senderPlayerData = ((ServerPlayerEntityAccess) context.getSource().getPlayerOrThrow()).ec$getPlayerData();
        String homeName = StringArgumentType.getString(context, "home_name");

        var targetPlayerName = StringArgumentType.getString(context, "target_player");
        ManagerLocator.getInstance()
            .getOfflinePlayerRepo()
            .getOfflinePlayerByNameAsync(targetPlayerName)
            .whenComplete((playerEntity, err) -> {
                if (playerEntity == null) {
                    context.getSource().sendError(Text.of("No player with the specified name found."));
                    return;
                }

                var targetPlayerData = ((ServerPlayerEntityAccess) playerEntity).ec$getPlayerData();

                try {
                    HomeCommand.exec(
                        senderPlayerData,
                        targetPlayerData,
                        homeName
                    );
                } catch (CommandSyntaxException e) {
                    context.getSource().sendError(ECText.access(senderPlayerData.getPlayer()).error(e.getMessage()));
                }
            });
        return SINGLE_SUCCESS;

    }

    public static int runListOffline(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var targetPlayerName = StringArgumentType.getString(context, "target_player");
        var senderPlayerProfile = PlayerProfile.accessFromContextOrThrow(context);
        ManagerLocator.getInstance()
            .getOfflinePlayerRepo()
            .getOfflinePlayerByNameAsync(targetPlayerName)
            .whenComplete((targetPlayerEntity, err) -> {
                if (targetPlayerEntity == null) {
                    context.getSource().sendError(Text.of("No player with the specified name found."));
                    return;
                }

                var targetPlayerData = ((ServerPlayerEntityAccess) targetPlayerEntity).ec$getPlayerData();
                var suggestionText = ListCommandFactory.getSuggestionText(
                    ECText.getInstance().getString("cmd.home.list.start"),
                    "home tp_offline %s".formatted(targetPlayerName),
                    targetPlayerData.getHomeEntries(),
                    Entry::getKey,
                    senderPlayerProfile
                );

                context.getSource().sendFeedback(() ->
                    suggestionText,
                    CONFIG.BROADCAST_TO_OPS
                );

            });
        return 0;
    }

    public static class Suggestion {
        //Brigader Suggestions
        public static final SuggestionProvider<ServerCommandSource> LIST_SUGGESTION_PROVIDER
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
        public static Set<Map.Entry<String, NamedMinecraftLocation>> getSuggestionEntries(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
            return HomeTeleportOtherCommand.getTargetPlayerData(context).getHomeEntries();
        }

    }
}
