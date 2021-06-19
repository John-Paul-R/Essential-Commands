package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.Config;
import com.fibermc.essentialcommands.ManagerLocator;
import com.fibermc.essentialcommands.TeleportRequestManager;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

import java.util.UUID;

public class TeleportAskCommand implements Command<ServerCommandSource> {

    public TeleportAskCommand() {}

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        TeleportRequestManager tpMgr = ManagerLocator.INSTANCE.getTpManager();
        //Store command sender
        ServerPlayerEntity senderPlayer = context.getSource().getPlayer();
        //Store Target Player
        ServerPlayerEntity targetPlayer = EntityArgumentType.getPlayer(context, "target");

        //inform target player of tp request via chat
        targetPlayer.sendSystemMessage(
                new LiteralText(senderPlayer.getEntityName()).formatted(Config.FORMATTING_ACCENT)
                        .append(new LiteralText(" has requested to teleport to you.")
                                .formatted(Config.FORMATTING_DEFAULT))
                        .append(new LiteralText("\nType '/tpaccept <name>' to accept or '/tpdeny <name>' to deny"
                                +" this request.").formatted(Config.FORMATTING_DEFAULT))
                , new UUID(0, 0)
        );
        
        //Mark TPRequest Sender as having requested a teleport
        tpMgr.startTpRequest(senderPlayer, targetPlayer);

        //inform command sender that request has been sent
        senderPlayer.sendSystemMessage(
                new LiteralText("Teleport request has been sent to ")
                        .formatted(Config.FORMATTING_DEFAULT)
                        .append(new LiteralText(targetPlayer.getEntityName())
                                .formatted(Config.FORMATTING_ACCENT))
                , new UUID(0, 0)
        );
        
        return 1;
    }
}
