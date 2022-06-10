package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.ECText;
import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.jpcode.eccore.util.TextUtil;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;

public class NicknameClearCommand implements Command<ServerCommandSource>  {
    public NicknameClearCommand() {}

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity targetPlayer = CommandUtil.getCommandTargetPlayer(context);
        ServerPlayerEntityAccess targetPlayerEntityAccess = (ServerPlayerEntityAccess) targetPlayer;
        targetPlayerEntityAccess.getEcPlayerData().setNickname(null);

        //inform command sender that the nickname has been set
        context.getSource().sendFeedback(TextUtil.concat(
            ECText.getInstance().getText("cmd.nickname.set.feedback").setStyle(CONFIG.FORMATTING_DEFAULT.getValue()),
            Text.literal(targetPlayer.getGameProfile().getName()),
            ECText.getInstance().getText("generic.quote_fullstop").setStyle(CONFIG.FORMATTING_DEFAULT.getValue())
        ), CONFIG.BROADCAST_TO_OPS.getValue());

        return 1;
    }

}
