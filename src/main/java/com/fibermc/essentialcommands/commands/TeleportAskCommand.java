package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.Config;
import com.fibermc.essentialcommands.TeleportRequestManager;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

import java.util.UUID;

public class TeleportAskCommand implements Command<ServerCommandSource> {

    private TeleportRequestManager tpMgr;
    public TeleportAskCommand(TeleportRequestManager tpMgr) {
        this.tpMgr = tpMgr;
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        
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
                , UUID.randomUUID()
        );
        
        //Mark TPRequest Sender as having requested a teleport
        tpMgr.startTpRequest(senderPlayer, targetPlayer);

        //inform command sender that request has been sent
        senderPlayer.sendSystemMessage(
                new LiteralText("Teleport request has been sent to ")
                        .formatted(Config.FORMATTING_DEFAULT)
                        .append(new LiteralText(targetPlayer.getEntityName())
                                .formatted(Config.FORMATTING_ACCENT))
                , UUID.randomUUID()
        );
        
        return 1;
    }
}
