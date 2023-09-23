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

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import dev.jpcode.eccore.util.TextUtil;

public class NearCommand implements Command<ServerCommandSource> {
    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        PlayerData senderPlayerData = PlayerData.accessFromContextOrThrow(context);
        ServerPlayerEntity targetPlayer = CommandUtil.getCommandTargetPlayer(context);

        return exec(senderPlayerData, targetPlayer, EssentialCommands.CONFIG.NEAR_COMMAND_DEFAULT_RADIUS);
    }

    public static int withRange(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        PlayerData senderPlayerData = PlayerData.accessFromContextOrThrow(context);
        ServerPlayerEntity targetPlayer = CommandUtil.getCommandTargetPlayer(context);

        int range = IntegerArgumentType.getInteger(context, "range");

        if (range > EssentialCommands.CONFIG.NEAR_COMMAND_MAX_RADIUS) {
            ECText ecTextSender = ECText.access(senderPlayerData.getPlayer());
            senderPlayerData.sendCommandError("cmd.near.error.range_too_high",
                ecTextSender.accent(String.valueOf(EssentialCommands.CONFIG.NEAR_COMMAND_MAX_RADIUS)));
            return 0;
        }

        return exec(senderPlayerData, targetPlayer, range);
    }

    public static int exec(PlayerData senderPlayerData, ServerPlayerEntity targetPlayer, int range) {
        Vec3d basePos = targetPlayer.getPos();

        List<Text> players = targetPlayer.getWorld().getPlayers().stream()
            .filter(player ->
                targetPlayer.getUuid() != player.getUuid()
                && basePos.isInRange(player.getPos(), range)
                && (!EssentialCommands.VANISH_PRESENT || VanishAPI.canSeePlayer((ServerPlayerEntity) player, senderPlayerData.getPlayer()))
            )
            .map(PlayerEntity::getDisplayName)
            .toList();

        if (players.isEmpty()) senderPlayerData.sendCommandFeedback("cmd.near.feedback.empty");
        else senderPlayerData.sendCommandFeedback("cmd.near.feedback.list", TextUtil.join(players, Text.literal(", ")));

        return SINGLE_SUCCESS;
    }
}
