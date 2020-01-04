package com.fibermc.essentialcommands;

import com.fibermc.essentialcommands.types.MinecraftLocation;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
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
        int out = 0;
        //Store command sender
        ServerPlayerEntity senderPlayer = context.getSource().getPlayer();
        PlayerData senderPlayerData = dataManager.getOrCreate(senderPlayer);
        //Store home name
        String homeName = StringArgumentType.getString(context, "home_name");
        
        //Get home location
        MinecraftLocation loc = senderPlayerData.getHomeLocation(homeName);

        //Teleport player to home location
        PlayerTeleporter.teleport(senderPlayerData, loc);

        //chat message
        if (loc != null) {
            senderPlayer.sendChatMessage(
                    new LiteralText("Teleporting to ").formatted(Formatting.byName(Config.FORMATTING_DEFAULT))
                            .append(new LiteralText(homeName).formatted(Formatting.byName(Config.FORMATTING_ACCENT)))
                            .append(new LiteralText("...").formatted(Formatting.byName(Config.FORMATTING_DEFAULT)))
                    , MessageType.SYSTEM);
            out=1;
        } else {
            senderPlayer.sendChatMessage(
                    new LiteralText("No home with the name '").formatted(Formatting.byName(Config.FORMATTING_ERROR))
                            .append(new LiteralText(homeName).formatted(Formatting.byName(Config.FORMATTING_ACCENT)))
                            .append(new LiteralText("' could be found.").formatted(Formatting.byName(Config.FORMATTING_ERROR)))
                    , MessageType.SYSTEM);

        }

        
        return out;
    }

}
