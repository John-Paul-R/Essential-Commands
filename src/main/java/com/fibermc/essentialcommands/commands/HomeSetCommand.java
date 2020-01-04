package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.Config;
import com.fibermc.essentialcommands.PlayerData;
import com.fibermc.essentialcommands.PlayerDataManager;
import com.fibermc.essentialcommands.types.MinecraftLocation;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.network.MessageType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

public class HomeSetCommand implements Command<ServerCommandSource> {

    private PlayerDataManager dataManager;
    public HomeSetCommand(PlayerDataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {

        //Store command sender
        ServerPlayerEntity senderPlayer = context.getSource().getPlayer();
        //Store home name
        String homeName = StringArgumentType.getString(context, "home_name");

        //Add home to PlayerData
        PlayerData pData = dataManager.getOrCreate(senderPlayer);
        int successCode = pData.addHome(homeName, new MinecraftLocation(senderPlayer));
        dataManager.savePlayerData(senderPlayer);
        //inform command sender that the home has been set
        if (successCode == 1) {
            senderPlayer.sendChatMessage(
                    new LiteralText("Home '").formatted(Config.FORMATTING_DEFAULT)
                            .append(new LiteralText(homeName).formatted(Config.FORMATTING_ACCENT))
                            .append(new LiteralText("' set.").formatted(Config.FORMATTING_DEFAULT))
                    , MessageType.SYSTEM);
        } else if (successCode==0) {
            senderPlayer.sendChatMessage(
                    new LiteralText("Home '").formatted(Config.FORMATTING_ERROR)
                            .append(new LiteralText(homeName).formatted(Config.FORMATTING_ACCENT))
                            .append(new LiteralText("' could not be set. Home limit (").formatted(Config.FORMATTING_ERROR))
                            .append(new LiteralText(String.valueOf(Config.HOME_LIMIT)).formatted(Config.FORMATTING_ACCENT))
                            .append(new LiteralText(") reached.").formatted(Config.FORMATTING_ERROR))
                    , MessageType.SYSTEM);
        }


        return successCode;
    }
}
