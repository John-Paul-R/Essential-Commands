package com.fibermc.essentialcommands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.network.MessageType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

public class TeleportDenyCommand implements Command<ServerCommandSource> {

    private PlayerDataManager dataManager;
    public TeleportDenyCommand(PlayerDataManager dataManager) {
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
            targetPlayer.sendChatMessage(
                new LiteralText("Teleport request denied.").formatted(Formatting.GOLD)
                , MessageType.SYSTEM);
            
            //Clean up TPAsk
            targetPlayerData.setTpTimer(-1);

            //Send message to command sender confiriming that request has been accepted
            senderPlayer.sendChatMessage(
                    new LiteralText("Teleport request denied.").formatted(Formatting.GOLD)
                , MessageType.SYSTEM);
            return 1;
        } else {
            //throw new CommandSyntaxException(type, message)
            return 0;
        }
    }
}
