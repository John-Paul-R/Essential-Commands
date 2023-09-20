package com.fibermc.essentialcommands.commands;

import java.util.Objects;

import com.fibermc.essentialcommands.text.ECText;
import com.fibermc.essentialcommands.text.TextFormatType;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;

public class FlySpeedCommand implements Command<ServerCommandSource> {
    static int speedMultiplier = 20;

    public FlySpeedCommand() {
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity targetPlayer = CommandUtil.getCommandTargetPlayer(context);

        int newSpeed = IntegerArgumentType.getInteger(context, "fly_speed");

        exec(source, targetPlayer, newSpeed);
        return SINGLE_SUCCESS;
    }

    public int reset(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity targetPlayer = CommandUtil.getCommandTargetPlayer(context);

        exec(source, targetPlayer, 1);
        return SINGLE_SUCCESS;
    }

    public static void exec(ServerCommandSource source, ServerPlayerEntity target, int flySpeed) throws CommandSyntaxException {
        ECText ecTextTarget = ECText.access(target);

        if (flySpeed > CONFIG.FLY_MAX_SPEED)
            throw CommandUtil.createSimpleException(
                ecTextTarget.getText(
                    "cmd.fly.speed.error.limit",
                    TextFormatType.Error,
                    ecTextTarget.accent(String.valueOf(CONFIG.FLY_MAX_SPEED))
                ));

        int oldFlySpeed = (int)(target.getAbilities().getFlySpeed() * speedMultiplier);
        target.getAbilities().setFlySpeed((float)flySpeed / speedMultiplier);
        target.sendAbilitiesUpdate();

        if (!Objects.equals(source.getPlayer(), target)) {
            ECText ecTextSource = ECText.access(source.getPlayer());
            source.sendFeedback(() ->
                ecTextSource.getText(
                    "cmd.fly.speed.feedback.update.other",
                    ecTextSource.accent(String.valueOf(oldFlySpeed)),
                    ecTextSource.accent(String.valueOf(flySpeed)),
                    target.getDisplayName()
                    ),
                CONFIG.BROADCAST_TO_OPS
            );
        }
        target.sendMessage(
            ecTextTarget.getText(
                "cmd.fly.speed.feedback.update",
                ecTextTarget.accent(String.valueOf(oldFlySpeed)),
                ecTextTarget.accent(String.valueOf(flySpeed))
            ));
    }
}
