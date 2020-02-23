package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.Config;
import com.fibermc.essentialcommands.PlayerData;
import com.fibermc.essentialcommands.PlayerDataManager;
import com.fibermc.essentialcommands.PlayerTeleporter;
import com.fibermc.essentialcommands.types.MinecraftLocation;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.network.MessageType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

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

        // Teleport & chat message
        if (loc != null) {
            senderPlayer.sendChatMessage(
                    new LiteralText("Teleporting to ").formatted(Config.FORMATTING_DEFAULT)
                            .append(new LiteralText(homeName).formatted(Config.FORMATTING_ACCENT))
                            .append(new LiteralText("...").formatted(Config.FORMATTING_DEFAULT))
                    , MessageType.SYSTEM);
            //Teleport player to home location
            PlayerTeleporter.teleport(senderPlayerData, loc);
            out=1;
        } else {
//            senderPlayer.sendChatMessage(
//                    new LiteralText("No home with the name '").formatted(Config.FORMATTING_ERROR)
//                            .append(new LiteralText(homeName).formatted(Config.FORMATTING_ACCENT))
//                            .append(new LiteralText("' could be found.").formatted(Config.FORMATTING_ERROR))
//                    , MessageType.SYSTEM);
            Message msg = new LiteralMessage("No home with the name '" + homeName + "' could be found.");
            throw new CommandSyntaxException(new SimpleCommandExceptionType(msg), msg);


        }

        
        return out;
    }

}
