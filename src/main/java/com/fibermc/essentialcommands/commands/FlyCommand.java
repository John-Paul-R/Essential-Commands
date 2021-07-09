package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.Config;
import com.fibermc.essentialcommands.util.TextUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

public class FlyCommand implements Command<ServerCommandSource> {

    public FlyCommand() {
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity senderPlayer = source.getPlayer();

        ServerPlayerEntity targetPlayer;
        try {
            targetPlayer = EntityArgumentType.getPlayer(context, "target_player");
        } catch (IllegalArgumentException e) {
            targetPlayer = senderPlayer;
        }

        exec(source, targetPlayer);
        return 0;
    }

    public void exec(ServerCommandSource source, ServerPlayerEntity target) {
        PlayerAbilities playerAbilities = target.getAbilities();

        playerAbilities.allowFlying = !playerAbilities.allowFlying;
        if (!playerAbilities.allowFlying) {
            playerAbilities.flying = false;
        }
        target.sendAbilitiesUpdate();

        source.sendFeedback(
            TextUtil.concat(
                new LiteralText("Flight ").setStyle(Config.FORMATTING_DEFAULT),
                new LiteralText(playerAbilities.allowFlying ? "enabled" : "disabled").setStyle(Config.FORMATTING_ACCENT),
                new LiteralText(" for ").setStyle(Config.FORMATTING_DEFAULT),
                target.getDisplayName(),
                new LiteralText(".").setStyle(Config.FORMATTING_DEFAULT)
            ),
            Config.BROADCAST_TO_OPS
        );
    }
}