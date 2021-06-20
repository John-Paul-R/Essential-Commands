package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.*;
import com.fibermc.essentialcommands.types.MinecraftLocation;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

import java.util.UUID;

public class TeleportAcceptCommand implements Command<ServerCommandSource> {

    public TeleportAcceptCommand() {}
    
    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        PlayerDataManager dataManager = ManagerLocator.INSTANCE.getPlayerDataManager();
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
                , new UUID(0, 0));

            //Conduct teleportation
            PlayerTeleporter.requestTeleport(new QueuedPlayerTeleport(
                targetPlayerData,
                senderPlayer,
                senderPlayer.getGameProfile().getName()
            ));

            //Clean up TPAsk
            targetPlayerData.setTpTimer(-1);

            //Send message to command sender confirming that request has been accepted
            senderPlayer.sendSystemMessage(
                    new LiteralText("Teleport request accepted.").formatted(Config.FORMATTING_DEFAULT)
                , new UUID(0, 0));
            return 1;
        } else {
            //throw new CommandSyntaxException(type, message)
            senderPlayer.sendSystemMessage(
                    new LiteralText("ERROR: Teleport failed.").formatted(Config.FORMATTING_ERROR)
                , new UUID(0, 0));
            return 0;
        }
    }

}
