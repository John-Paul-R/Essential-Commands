package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.ECAbilitySources;
import com.fibermc.essentialcommands.ECText;
import com.fibermc.essentialcommands.TextFormatType;
import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import io.github.ladysnake.pal.Pal;
import io.github.ladysnake.pal.VanillaAbilities;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class InvulnCommand implements Command<ServerCommandSource> {
    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity targetPlayer = CommandUtil.getCommandTargetPlayer(context);

        boolean shouldEnableInvuln;
        try {
            // Prefer explicitly specified flight state from commands args...
            shouldEnableInvuln = BoolArgumentType.getBool(context, "invuln_enabled");
        } catch (IllegalArgumentException e) {
            shouldEnableInvuln = !VanillaAbilities.INVULNERABLE
                .getTracker(targetPlayer).isGrantedBy(ECAbilitySources.INVULN_COMMAND);
        }

        exec(targetPlayer, shouldEnableInvuln);

        // TODO Label boolean values in suggestions, or switch to single state value (present, or it's not)

        var senderPlayerAccess = ((ServerPlayerEntityAccess) source.getPlayerOrThrow());
        var enabledText = ECText.getInstance().getText(
            shouldEnableInvuln ? "generic.enabled" : "generic.disabled",
            TextFormatType.Accent,
            senderPlayerAccess.ec$getProfile());

        senderPlayerAccess.ec$getPlayerData().sendCommandFeedback(
            "cmd.invuln.feedback",
            enabledText,
            targetPlayer.getDisplayName()
        );

        return 0;
    }

    public static void exec(ServerPlayerEntity target, boolean shouldEnableInvuln) throws CommandSyntaxException {
        if (shouldEnableInvuln) {
            Pal.grantAbility(target, VanillaAbilities.INVULNERABLE, ECAbilitySources.INVULN_COMMAND);
        } else {
            Pal.revokeAbility(target, VanillaAbilities.INVULNERABLE, ECAbilitySources.INVULN_COMMAND);
        }
        target.sendAbilitiesUpdate();
    }
}
