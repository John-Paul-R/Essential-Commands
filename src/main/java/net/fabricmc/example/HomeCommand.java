package net.fabricmc.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

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
        //Store home name
        String homeName = StringArgumentType.getString(context, "home_name");
        
        //Get home location
        MinecraftLocation loc = dataManager.getOrCreatePlayerData(senderPlayer).getHomeLocation(homeName);

        //Teleport player to home location
        senderPlayer.teleport(loc.world, loc.x, loc.y, loc.z, 0f, 0f);

        //inform command sender that the home has been set
        senderPlayer.sendChatMessage(
                new LiteralText("Teleporting to ").formatted(Formatting.GOLD)
                    .append(new LiteralText(homeName).formatted(Formatting.LIGHT_PURPLE))
                    .append(new LiteralText("...").formatted(Formatting.GOLD))
            , MessageType.SYSTEM);
        
        return 1;
    }

    public SuggestionProvider<ServerCommandSource> suggestedStrings() {
        return (context, builder) -> getSuggestionsBuilder(builder, 
            new ArrayList<String>(dataManager.getOrCreatePlayerData(context.getSource().getPlayer()).getHomeNames()));
    }
     
    private CompletableFuture<Suggestions> getSuggestionsBuilder(SuggestionsBuilder builder, List<String> list) {
        String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
     
        if(list.isEmpty()) { // If the list is empty then return no suggestions
            return Suggestions.empty(); // No suggestions
        }
     
        for (String str : list) { // Iterate through the supplied list
            if (str.toLowerCase(Locale.ROOT).startsWith(remaining)) {
                builder.suggest(str); // Add every single entry to suggestions list.
            }
        }
        return builder.buildFuture(); // Create the CompletableFuture containing all the suggestions
    }
}
