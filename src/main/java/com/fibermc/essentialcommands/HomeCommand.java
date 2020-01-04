package com.fibermc.essentialcommands;

import java.util.ArrayList;

import com.fibermc.essentialcommands.types.MinecraftLocation;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.network.MessageType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

public class HomeCommand implements Command<ServerCommandSource> {

    private PlayerDataManager dataManager;
    public HomeCommand(PlayerDataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        
        //Store command sender
        ServerPlayerEntity senderPlayer = context.getSource().getPlayer();
        PlayerData senderPlayerData = dataManager.getOrCreate(senderPlayer);
        //Store home name
        String homeName = StringArgumentType.getString(context, "home_name");
        
        //Get home location
        MinecraftLocation loc = senderPlayerData.getHomeLocation(homeName);

        //Teleport player to home location
        PlayerTeleporter.teleport(senderPlayerData, loc);

        //inform command sender that the home has been set
        senderPlayer.sendChatMessage(
                new LiteralText("Teleporting to ").formatted(Formatting.byName(Prefs.FORMATTING_DEFAULT))
                    .append(new LiteralText(homeName).formatted(Formatting.byName(Prefs.FORMATTING_ACCENT)))
                    .append(new LiteralText("...").formatted(Formatting.byName(Prefs.FORMATTING_DEFAULT)))
            , MessageType.SYSTEM);
        
        return 1;
    }

    //Brigader Suggestions
    public SuggestionProvider<ServerCommandSource> suggestedStrings() {
        return (context, builder) -> ListSuggestion.getSuggestionsBuilder(builder,
            new ArrayList<String>(dataManager.getOrCreate(context.getSource().getPlayer()).getHomeNames()));
    }

}
