package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.ECAbilitySources;
import com.fibermc.essentialcommands.ECText;
import com.fibermc.essentialcommands.PlayerData;
import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.util.TextUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.ladysnake.pal.VanillaAbilities;
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

    public static void exec(ServerCommandSource source, ServerPlayerEntity target, boolean shouldEnableFly) {
        PlayerAbilities playerAbilities = target.getAbilities();

        PlayerData playerData = ((ServerPlayerEntityAccess) target).getEcPlayerData();

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

        source.sendFeedback(
            TextUtil.concat(
                ECText.getInstance().getText("cmd.fly.feedback.1").setStyle(CONFIG.FORMATTING_DEFAULT.getValue()),
                new LiteralText(shouldEnableFly ? "enabled" : "disabled").setStyle(CONFIG.FORMATTING_ACCENT.getValue()),
                ECText.getInstance().getText("cmd.fly.feedback.2").setStyle(CONFIG.FORMATTING_DEFAULT.getValue()),
                target.getDisplayName(),
                new LiteralText(".").setStyle(CONFIG.FORMATTING_DEFAULT.getValue())
            ),
            CONFIG.BROADCAST_TO_OPS.getValue()
        );
    }
}