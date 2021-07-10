package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.config.Config;
import com.fibermc.essentialcommands.PlayerData;
import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Util;

public class TeleportDenyCommand implements Command<ServerCommandSource> {

    public TeleportDenyCommand() {}

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        //Store command sender
         ServerPlayerEntity senderPlayer = source.getPlayer();
        //Store Target Player
         ServerPlayerEntity targetPlayer = EntityArgumentType.getPlayer(context, "target");
         PlayerData targetPlayerData = ((ServerPlayerEntityAccess)targetPlayer).getEcPlayerData();

        //identify if target player did indeed request to teleport. Continue if so, otherwise throw exception.
        if (targetPlayerData.getTpTarget().getPlayer().equals(senderPlayer)) {
            //inform target player that teleport has been accepted via chat
            targetPlayer.sendSystemMessage(
                new LiteralText("Teleport request denied.").setStyle(Config.FORMATTING_DEFAULT)
                , Util.NIL_UUID);
            
            //Clean up TPAsk
            targetPlayerData.setTpTimer(-1);

            //Send message to command sender confirming that request has been accepted
            source.sendFeedback(
                new LiteralText("Teleport request denied.").setStyle(Config.FORMATTING_DEFAULT)
                , Config.BROADCAST_TO_OPS
            );
            return 1;
        } else {
            //throw new CommandSyntaxException(type, message)
            return 0;
        }
    }
}
