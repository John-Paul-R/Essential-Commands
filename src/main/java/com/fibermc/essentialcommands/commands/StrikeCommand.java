package com.fibermc.essentialcommands.commands;

import java.util.Objects;

import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.playerdata.PlayerData;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LightningEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class StrikeCommand implements Command<ServerCommandSource> {

    public StrikeCommand() {
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity targetPlayer = CommandUtil.getCommandTargetPlayer(context);

        //Tries to spawn a LightningBolt entity at the target player location
        LightningEntity lightningEntity = new LightningEntity(EntityType.LIGHTNING_BOLT, targetPlayer.getServerWorld());
        //TODO what's lightningEntity.setCosmetic(); ?
        lightningEntity.setPos(targetPlayer.getX(), targetPlayer.getY(), targetPlayer.getZ());

        exec(source, targetPlayer, lightningEntity);
        return 0;
    }

    public static void exec(ServerCommandSource source, ServerPlayerEntity target, LightningEntity entity) throws CommandSyntaxException {

        PlayerData playerData = ((ServerPlayerEntityAccess) target).ec$getPlayerData();

        target.sendAbilitiesUpdate();
        target.getWorld().spawnEntity(entity);

        var senderPlayer = source.getPlayerOrThrow();
        var senderPlayerData = PlayerData.access(senderPlayer);

        if (!Objects.equals(senderPlayer, target)) {
            senderPlayerData.sendCommandFeedback(
                "cmd.strike.feedback",
                target.getDisplayName());
        }
        playerData.sendCommandFeedback(
            "cmd.strike.feedback",
            target.getDisplayName()
        );
    }
}
