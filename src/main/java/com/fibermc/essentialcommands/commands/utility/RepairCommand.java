package com.fibermc.essentialcommands.commands.utility;

import com.fibermc.essentialcommands.commands.CommandUtil;
import com.fibermc.essentialcommands.playerdata.PlayerData;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class RepairCommand implements Command<CommandSourceStack> {
    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        PlayerData senderPlayerData = PlayerData.accessFromContextOrThrow(context);
        ServerPlayer targetPlayer = CommandUtil.getCommandTargetPlayer(context);

        ItemStack itemStack = targetPlayer.getMainHandItem();

        if (!itemStack.isDamaged()) {
            senderPlayerData.sendCommandError("cmd.repair.error.not_damaged");
            return 0;
        }

        itemStack.setDamageValue(0);

        senderPlayerData.sendCommandFeedback("cmd.repair.feedback");

        return SINGLE_SUCCESS;
    }
}
