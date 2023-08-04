package com.fibermc.essentialcommands.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.commands.suggestions.ListSuggestion;
import com.fibermc.essentialcommands.playerdata.PlayerData;
import com.fibermc.essentialcommands.teleportation.PlayerTeleporter;
import com.fibermc.essentialcommands.text.ECText;
import com.fibermc.essentialcommands.text.TextFormatType;
import com.fibermc.essentialcommands.types.MinecraftLocation;
import com.fibermc.essentialcommands.types.NamedMinecraftLocation;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class HomeCommand implements Command<ServerCommandSource> {

    public HomeCommand() {}

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        PlayerData senderPlayerData = ((ServerPlayerEntityAccess) context.getSource().getPlayerOrThrow()).ec$getPlayerData();
        String homeName = StringArgumentType.getString(context, "home_name");

        return exec(senderPlayerData, homeName);
    }

    private static PlayerData getTargetPlayerData(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return ((ServerPlayerEntityAccess) context.getSource().getPlayerOrThrow()).ec$getPlayerData();
    }

    // TODO: Ideally the styling here should come from a context, intead of from the player we're
    //  accessing, but I don't think it matters, practically speaking, for now.
    public static String getSoleHomeName(PlayerData playerData) throws CommandSyntaxException {
        Set<String> homeNames = playerData.getHomeNames();
        var ecText = ECText.access(playerData.getPlayer());
        if (homeNames.size() > 1) {
            throw CommandUtil.createSimpleException(
                ecText.getText("cmd.home.tp.error.shortcut_more_than_one", TextFormatType.Error));
        } else if (homeNames.isEmpty()) {
            throw CommandUtil.createSimpleException(
                ecText.getText("cmd.home.tp.error.shortcut_none_exist", TextFormatType.Error));
        }

        return homeNames.stream().findAny().get();
    }

    public int runDefault(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        PlayerData playerData = ((ServerPlayerEntityAccess) context.getSource().getPlayerOrThrow()).ec$getPlayerData();

        return exec(
            playerData,
            getSoleHomeName(playerData)
        );
    }

    private static int exec(PlayerData senderPlayerData, String homeName) throws CommandSyntaxException {
        return exec(senderPlayerData, senderPlayerData, homeName);
    }

    public static int exec(PlayerData senderPlayerData, PlayerData targetPlayerData, String homeName) throws CommandSyntaxException {
        //Get home location
        MinecraftLocation loc = targetPlayerData.getHomeLocation(homeName);
        var ecText = ECText.access(senderPlayerData.getPlayer());
        if (loc == null) {
            Message msg = ecText.getText(
                "cmd.home.tp.error.not_found",
                TextFormatType.Error,
                Text.literal(homeName));
            throw new CommandSyntaxException(new SimpleCommandExceptionType(msg), msg);
        }

        // Teleport & chat message
        var homeNameText = ecText.getText(
            "cmd.home.location_name",
            TextFormatType.Default,
            ecText.accent(homeName));

        PlayerTeleporter.requestTeleport(senderPlayerData, loc, homeNameText);
        return SINGLE_SUCCESS;
    }

    public static class Suggestion {
        //Brigader Suggestions
        public static final SuggestionProvider<ServerCommandSource> LIST_SUGGESTION_PROVIDER
            = ListSuggestion.ofContext(Suggestion::getSuggestionsList);

        /**
         * Gets a list of suggested strings to be used with Brigader
         */
        public static List<String> getSuggestionsList(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
            return new ArrayList<>(HomeCommand.getTargetPlayerData(context).getHomeNames());
        }

        /**
         * Gets a set of suggestion entries to be used with ListCommandFactory
         */
        public static Set<Map.Entry<String, NamedMinecraftLocation>> getSuggestionEntries(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
            return HomeCommand.getTargetPlayerData(context).getHomeEntries();
        }
    }
}
