package com.fibermc.essentialcommands.commands.utility;

import java.util.List;

import com.fibermc.essentialcommands.EssentialCommands;
import com.fibermc.essentialcommands.commands.CommandUtil;
import com.fibermc.essentialcommands.playerdata.PlayerData;
import com.fibermc.essentialcommands.text.ECText;
import me.drex.vanish.api.VanishAPI;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import dev.jpcode.eccore.util.TextUtil;

public class NearCommand implements Command<CommandSourceStack> {
    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        PlayerData senderPlayerData = PlayerData.accessFromContextOrThrow(context);
        ServerPlayer targetPlayer = CommandUtil.getCommandTargetPlayer(context);

        return exec(senderPlayerData, targetPlayer, EssentialCommands.CONFIG.NEAR_COMMAND_DEFAULT_RADIUS);
    }

    public static int withRange(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        PlayerData senderPlayerData = PlayerData.accessFromContextOrThrow(context);
        ServerPlayer targetPlayer = CommandUtil.getCommandTargetPlayer(context);

        int range = IntegerArgumentType.getInteger(context, "range");

        if (range > EssentialCommands.CONFIG.NEAR_COMMAND_MAX_RADIUS) {
            ECText ecTextSender = ECText.access(senderPlayerData.getPlayer());
            senderPlayerData.sendCommandError("cmd.near.error.range_too_high",
                ecTextSender.accent(String.valueOf(EssentialCommands.CONFIG.NEAR_COMMAND_MAX_RADIUS)));
            return 0;
        }

        return exec(senderPlayerData, targetPlayer, range);
    }

    public static int exec(PlayerData senderPlayerData, ServerPlayer targetPlayer, int range) {
        Vec3 basePos = targetPlayer.position();

        List<Component> players = targetPlayer.level().players().stream()
            .filter(player ->
                targetPlayer.getUUID() != player.getUUID()
                && basePos.closerThan(player.position(), range)
                && (!EssentialCommands.VANISH_PRESENT || VanishAPI.canSeePlayer((ServerPlayer) player, senderPlayerData.getPlayer()))
            )
            .map(Player::getDisplayName)
            .toList();

        if (players.isEmpty()) senderPlayerData.sendCommandFeedback("cmd.near.feedback.empty");
        else senderPlayerData.sendCommandFeedback("cmd.near.feedback.list", TextUtil.join(players, Component.literal(", ")));

        return SINGLE_SUCCESS;
    }
}
