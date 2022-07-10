package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.ECText;
import com.fibermc.essentialcommands.PlayerData;
import com.fibermc.essentialcommands.PlayerTeleporter;
import com.fibermc.essentialcommands.TextFormatType;
import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.commands.suggestions.ListSuggestion;
import com.fibermc.essentialcommands.types.MinecraftLocation;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HomeCommand implements Command<ServerCommandSource> {

    public HomeCommand() {}

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        PlayerData senderPlayerData = ((ServerPlayerEntityAccess)context.getSource().getPlayer()).getEcPlayerData();
        String homeName = StringArgumentType.getString(context, "home_name");

        return exec(senderPlayerData, homeName);
    }

    private static PlayerData getTargetPlayerData(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        return ((ServerPlayerEntityAccess)context.getSource().getPlayer()).getEcPlayerData();
    }

    public static String getSoleHomeName(PlayerData playerData) throws CommandSyntaxException {
        Set<String> homeNames = playerData.getHomeNames();
        if (homeNames.size() > 1) {
            throw CommandUtil.createSimpleException(ECText.getInstance().getText("cmd.home.tp.error.shortcut_more_than_one"));
        } else if (homeNames.isEmpty()) {
            throw CommandUtil.createSimpleException(ECText.getInstance().getText("cmd.home.tp.error.shortcut_none_exist"));
        }

        return homeNames.stream().findAny().get();
    }

    public int runDefault(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        PlayerData playerData = ((ServerPlayerEntityAccess) context.getSource().getPlayer()).getEcPlayerData();

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

        if (loc == null) {
            Message msg = ECText.getInstance().getText(
                "cmd.home.tp.error.not_found",
                TextFormatType.Error,
                Text.literal(homeName));
            throw new CommandSyntaxException(new SimpleCommandExceptionType(msg), msg);
        }

        // Teleport & chat message
        PlayerTeleporter.requestTeleport(
            senderPlayerData,
            loc,
            ECText.getInstance().getText("cmd.home.location_name", Text.literal(homeName)));
        return 1;
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
        public static Set<Map.Entry<String, MinecraftLocation>> getSuggestionEntries(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
            return HomeCommand.getTargetPlayerData(context).getHomeEntries();
        }

    }
}
