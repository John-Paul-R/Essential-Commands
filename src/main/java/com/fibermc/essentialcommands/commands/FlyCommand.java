package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.ECText;
import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.util.TextUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;


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

        boolean permanent;
        try {
            permanent = BoolArgumentType.getBool(context, "permanent");
        } catch (IllegalArgumentException e) {
            permanent = false;
        }

        exec(source, targetPlayer, permanent);
        return 0;
    }

    public static void exec(ServerCommandSource source, ServerPlayerEntity target, boolean permanent) {
        PlayerAbilities playerAbilities = target.getAbilities();

        playerAbilities.allowFlying = !playerAbilities.allowFlying;
        if (!playerAbilities.allowFlying) {
            playerAbilities.flying = false;
        }
        ((ServerPlayerEntityAccess) target).getEcPlayerData().setPersistFlight(permanent);
        target.sendAbilitiesUpdate();

        source.sendFeedback(
            TextUtil.concat(
                ECText.getInstance().getText("cmd.fly.feedback.1").setStyle(CONFIG.FORMATTING_DEFAULT.getValue()),
                new LiteralText(playerAbilities.allowFlying ? "enabled" : "disabled").setStyle(CONFIG.FORMATTING_ACCENT.getValue()),
                ECText.getInstance().getText("cmd.fly.feedback.2").setStyle(CONFIG.FORMATTING_DEFAULT.getValue()),
                target.getDisplayName(),
                new LiteralText(".").setStyle(CONFIG.FORMATTING_DEFAULT.getValue())
            ),
            CONFIG.BROADCAST_TO_OPS.getValue()
        );
    }
}