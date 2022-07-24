package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.ECText;
import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;

public class NicknameClearCommand implements Command<ServerCommandSource> {
    public NicknameClearCommand() {}

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerPlayerEntity targetPlayer = CommandUtil.getCommandTargetPlayer(context);
        ServerPlayerEntityAccess targetPlayerEntityAccess = (ServerPlayerEntityAccess) targetPlayer;
        targetPlayerEntityAccess.getEcPlayerData().setNickname(null);

        //inform command sender that the nickname has been set
        context.getSource().sendFeedback(
            ECText.getInstance().getText(
                "cmd.nickname.set.feedback",
                Text.literal(targetPlayer.getGameProfile().getName())),
            CONFIG.BROADCAST_TO_OPS);

        return 1;
    }

}
