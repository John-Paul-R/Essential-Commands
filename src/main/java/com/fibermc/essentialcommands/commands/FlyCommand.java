package com.fibermc.essentialcommands.commands;

import java.util.Objects;

import com.fibermc.essentialcommands.ECAbilitySources;
import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.playerdata.PlayerData;
import com.fibermc.essentialcommands.text.ECText;
import io.github.ladysnake.pal.VanillaAbilities;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Abilities;

public class FlyCommand implements Command<CommandSourceStack> {

    public FlyCommand() {
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer targetPlayer = CommandUtil.getCommandTargetPlayer(context);

        boolean shouldEnableFly;
        try {
            // Prefer explicitly specified flight state from commands args...
            shouldEnableFly = BoolArgumentType.getBool(context, "flight_enabled");
        } catch (IllegalArgumentException e) {
            try {
                // Fall back to toggling the current PAL flight state granted by EC
                shouldEnableFly = !VanillaAbilities.ALLOW_FLYING
                    .getTracker(targetPlayer).isGrantedBy(ECAbilitySources.FLY_COMMAND);
            } catch (NoClassDefFoundError ign) {
                // If PAL is not found, fall back to toggling the current vanilla flight state.
                shouldEnableFly = !targetPlayer.getAbilities().mayfly;
            }
        }

        exec(source, targetPlayer, shouldEnableFly);
        return 0;
    }

    public static void exec(CommandSourceStack source, ServerPlayer target, boolean shouldEnableFly) throws CommandSyntaxException {
        Abilities playerAbilities = target.getAbilities();

        PlayerData playerData = ((ServerPlayerEntityAccess) target).ec$getPlayerData();

        try {
            playerData.setFlight(shouldEnableFly);
        } catch (NoClassDefFoundError ign) {
            playerAbilities.mayfly = shouldEnableFly;
            if (!shouldEnableFly) {
                playerAbilities.flying = false;
            }
        }

        target.onUpdateAbilities();

        // Label boolean values in suggestions, or switch to single state value (present or it's not)

        var senderPlayer = source.getPlayerOrException();
        var senderPlayerData = PlayerData.access(senderPlayer);
        var ecTextTarget = ECText.access(target);
        String enabledString = ecTextTarget.getString(shouldEnableFly ? "generic.enabled" : "generic.disabled");

        if (!Objects.equals(senderPlayer, target)) {
            ECText ecTextSender = ECText.access(senderPlayer);
            senderPlayerData.sendCommandFeedback(
                "cmd.fly.feedback",
                ecTextSender.accent(enabledString),
                target.getDisplayName());
        }
        playerData.sendCommandFeedback(
            "cmd.fly.feedback",
            ecTextTarget.accent(enabledString),
            target.getDisplayName()
        );
    }
}
