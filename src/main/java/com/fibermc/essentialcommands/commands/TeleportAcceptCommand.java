package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.Config;
import com.fibermc.essentialcommands.PlayerData;
import com.fibermc.essentialcommands.PlayerDataManager;
import com.fibermc.essentialcommands.PlayerTeleporter;
import com.fibermc.essentialcommands.types.MinecraftLocation;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

import java.util.UUID;

public class TeleportAcceptCommand implements Command<ServerCommandSource> {

    private PlayerDataManager dataManager;
    public TeleportAcceptCommand(PlayerDataManager dataManager) {
        this.dataManager = dataManager;
    }
    
    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {

        //Store command sender
        ServerPlayerEntity senderPlayer = context.getSource().getPlayer();
        //Store Target Player
        ServerPlayerEntity targetPlayer = EntityArgumentType.getPlayer(context, "target");
        PlayerData targetPlayerData = dataManager.getOrCreate(targetPlayer);

        //identify if target player did indeed request to teleport. Continue if so, otherwise throw exception.
        if (targetPlayerData.getTpTarget().getPlayer().equals(senderPlayer)) {

            //inform target player that teleport has been accepted via chat
            targetPlayer.sendSystemMessage(
                new LiteralText("Teleport request accepted.").formatted(Config.FORMATTING_DEFAULT)
                , UUID.randomUUID());

            //Conduct teleportation
            PlayerTeleporter.teleport(targetPlayerData, new MinecraftLocation(senderPlayer));

            //Clean up TPAsk
            targetPlayerData.setTpTimer(-1);
            ////tpManager.endTpRequest(targetPlayer);

            //Send message to command sender confirming that request has been accepted
            senderPlayer.sendSystemMessage(
                    new LiteralText("Teleport request accepted.").formatted(Config.FORMATTING_DEFAULT)
                , UUID.randomUUID());
            return 1;
        } else {
            //throw new CommandSyntaxException(type, message)
            senderPlayer.sendSystemMessage(
                    new LiteralText("ERROR: Teleport failed.").formatted(Config.FORMATTING_ERROR)
                , UUID.randomUUID());
            return 0;
        }
    }

}
