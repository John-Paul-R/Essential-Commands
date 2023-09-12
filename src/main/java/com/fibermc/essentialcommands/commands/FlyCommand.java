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

import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class FlyCommand implements Command<ServerCommandSource> {

    public FlyCommand() {
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity targetPlayer = CommandUtil.getCommandTargetPlayer(context);

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
                shouldEnableFly = !targetPlayer.getAbilities().allowFlying;
            }
        }

        exec(source, targetPlayer, shouldEnableFly);
        return 0;
    }

    public static void exec(ServerCommandSource source, ServerPlayerEntity target, boolean shouldEnableFly) throws CommandSyntaxException {
        PlayerAbilities playerAbilities = target.getAbilities();

        PlayerData playerData = ((ServerPlayerEntityAccess) target).ec$getPlayerData();

        try {
            playerData.setFlight(shouldEnableFly);
        } catch (NoClassDefFoundError ign) {
            playerAbilities.allowFlying = shouldEnableFly;
            if (!shouldEnableFly) {
                playerAbilities.flying = false;
            }
        }

        target.sendAbilitiesUpdate();

        // Label boolean values in suggestions, or switch to single state value (present or it's not)

        var senderPlayer = source.getPlayerOrThrow();
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
