package com.fibermc.essentialcommands.commands.utility;

import com.fibermc.essentialcommands.commands.CommandUtil;
import com.fibermc.essentialcommands.playerdata.PlayerData;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class RepairCommand implements Command<ServerCommandSource> {
    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        PlayerData senderPlayerData = PlayerData.accessFromContextOrThrow(context);
        ServerPlayerEntity targetPlayer = CommandUtil.getCommandTargetPlayer(context);

        ItemStack itemStack = targetPlayer.getMainHandStack();

        if (!itemStack.isDamaged()) {
            senderPlayerData.sendCommandError("cmd.repair.error.not_damaged");
            return 0;
        }

        itemStack.setDamage(0);

        senderPlayerData.sendCommandFeedback("cmd.repair.feedback");

        return SINGLE_SUCCESS;
    }
}
