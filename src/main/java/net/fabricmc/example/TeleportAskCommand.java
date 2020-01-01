package net.fabricmc.example;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.arguments.EntityArgumentType;
import net.minecraft.network.MessageType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

public class TeleportAskCommand implements Command<ServerCommandSource> {

    private PlayerDataManager dataManager;
    public TeleportAskCommand(PlayerDataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        
        //Store command sender
        ServerPlayerEntity senderPlayer = context.getSource().getPlayer();
        //Store Target Player
        ServerPlayerEntity targetPlayer = EntityArgumentType.getPlayer(context, "target");

        //inform target player of tp request via chat
        targetPlayer.sendChatMessage(
            new LiteralText(senderPlayer.getEntityName()).formatted(Formatting.LIGHT_PURPLE)
                .append(new LiteralText(" has requested to teleport to you.").formatted(Formatting.GOLD))
                .append(new LiteralText("\nType '/tpaccept <name>' to accept or '/tpdeny <name>' to deny this request.").formatted(Formatting.GOLD))
            , MessageType.SYSTEM);
        
        //Mark TPRequest Sender as having requested a teleport
        dataManager.handleTpRequest(senderPlayer, targetPlayer);
        //// CompoundTag tpTimerTag = new CompoundTag();
        //// tpTimerTag.putInt("tpTimer", 60);
        //// senderPlayer.writeCustomDataToTag(tpTimerTag);

        //inform command sender that request has been sent
        senderPlayer.sendChatMessage(
                new LiteralText("Teleport request has been sent to ").formatted(Formatting.GOLD)
                    .append(new LiteralText(targetPlayer.getEntityName()).formatted(Formatting.LIGHT_PURPLE))
            , MessageType.SYSTEM);
        
        return 1;
    }
}
