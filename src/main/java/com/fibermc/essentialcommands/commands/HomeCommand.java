package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.*;
import com.fibermc.essentialcommands.types.MinecraftLocation;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

import java.util.UUID;

public class HomeCommand implements Command<ServerCommandSource> {

    public HomeCommand() {}

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        PlayerDataManager dataManager = ManagerLocator.INSTANCE.getPlayerDataManager();
        int out;
        //Store command sender
        ServerPlayerEntity senderPlayer = context.getSource().getPlayer();
        PlayerData senderPlayerData = dataManager.getOrCreate(senderPlayer);
        //Store home name
        String homeName = StringArgumentType.getString(context, "home_name");
        
        //Get home location
        MinecraftLocation loc = senderPlayerData.getHomeLocation(homeName);

        // Teleport & chat message
        if (loc != null) {
            senderPlayer.sendSystemMessage(
                    new LiteralText("Teleporting to ").formatted(Config.FORMATTING_DEFAULT)
                            .append(new LiteralText(homeName).formatted(Config.FORMATTING_ACCENT))
                            .append(new LiteralText("...").formatted(Config.FORMATTING_DEFAULT))
                    , new UUID(0, 0));
            //Teleport player to home location
            PlayerTeleporter.teleport(senderPlayerData, loc);
            out=1;
        } else {
//            senderPlayer.sendSystemMessage(
//                    new LiteralText("No home with the name '").formatted(Config.FORMATTING_ERROR)
//                            .append(new LiteralText(homeName).formatted(Config.FORMATTING_ACCENT))
//                            .append(new LiteralText("' could be found.").formatted(Config.FORMATTING_ERROR))
//                    , new UUID(0, 0));
            Message msg = new LiteralMessage("No home with the name '" + homeName + "' could be found.");
            throw new CommandSyntaxException(new SimpleCommandExceptionType(msg), msg);


        }

        
        return out;
    }

}
