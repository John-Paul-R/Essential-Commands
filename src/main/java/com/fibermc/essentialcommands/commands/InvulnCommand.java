package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.ECAbilitySources;
import com.fibermc.essentialcommands.ECText;
import com.fibermc.essentialcommands.TextFormatType;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.ladysnake.pal.Pal;
import io.github.ladysnake.pal.VanillaAbilities;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;

public class InvulnCommand implements Command<ServerCommandSource> {

    public InvulnCommand() {
    }

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

        exec(source, targetPlayer, shouldEnableInvuln);
        return 0;
    }

    public static void exec(ServerCommandSource source, ServerPlayerEntity target, boolean shouldEnableInvuln) {
        if (shouldEnableInvuln) {
            Pal.grantAbility(target, VanillaAbilities.INVULNERABLE, ECAbilitySources.INVULN_COMMAND);
        } else {
            Pal.revokeAbility(target, VanillaAbilities.INVULNERABLE, ECAbilitySources.INVULN_COMMAND);
        }
        target.sendAbilitiesUpdate();

        // Label boolean values in suggestions, or switch to single state value (present or it's not)

        var enabledText = ECText.getInstance().getText(
            shouldEnableInvuln ? "generic.enabled" : "generic.disabled",
            TextFormatType.Accent);

        source.sendFeedback(
            ECText.getInstance().getText(
                "cmd.invuln.feedback",
                enabledText,
                target.getDisplayName()
            ),
            CONFIG.BROADCAST_TO_OPS
        );
    }
}