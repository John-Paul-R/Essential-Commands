package com.fibermc.essentialcommands.commands;

import java.util.Objects;

import com.fibermc.essentialcommands.text.ECText;
import com.fibermc.essentialcommands.text.TextFormatType;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;

public class FlySpeedCommand implements Command<CommandSourceStack> {
    static int speedMultiplier = 20;

    public FlySpeedCommand() {
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer targetPlayer = CommandUtil.getCommandTargetPlayer(context);

        int newSpeed = IntegerArgumentType.getInteger(context, "fly_speed");

        exec(source, targetPlayer, newSpeed);
        return SINGLE_SUCCESS;
    }

    public int reset(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer targetPlayer = CommandUtil.getCommandTargetPlayer(context);

        exec(source, targetPlayer, 1);
        return SINGLE_SUCCESS;
    }

    public static void exec(CommandSourceStack source, ServerPlayer target, int flySpeed) throws CommandSyntaxException {
        ECText ecTextTarget = ECText.access(target);

        if (flySpeed > CONFIG.FLY_MAX_SPEED)
            throw CommandUtil.createSimpleException(
                ecTextTarget.getText(
                    "cmd.fly.speed.error.limit",
                    TextFormatType.Error,
                    ecTextTarget.accent(String.valueOf(CONFIG.FLY_MAX_SPEED))
                ));

        int oldFlySpeed = (int)(target.getAbilities().getFlyingSpeed() * speedMultiplier);
        target.getAbilities().setFlyingSpeed((float)flySpeed / speedMultiplier);
        target.onUpdateAbilities();

        if (!Objects.equals(source.getPlayer(), target)) {
            ECText ecTextSource = ECText.access(source.getPlayer());
            source.sendSuccess(() ->
                ecTextSource.getText(
                    "cmd.fly.speed.feedback.update.other",
                    ecTextSource.accent(String.valueOf(oldFlySpeed)),
                    ecTextSource.accent(String.valueOf(flySpeed)),
                    target.getDisplayName()
                    ),
                CONFIG.BROADCAST_TO_OPS
            );
        }
        target.sendSystemMessage(
            ecTextTarget.getText(
                "cmd.fly.speed.feedback.update",
                ecTextTarget.accent(String.valueOf(oldFlySpeed)),
                ecTextTarget.accent(String.valueOf(flySpeed))
            ));
    }
}
