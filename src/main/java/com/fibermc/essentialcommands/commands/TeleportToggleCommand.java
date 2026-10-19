package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.playerdata.PlayerData;
import com.fibermc.essentialcommands.playerdata.PlayerProfile;
import com.fibermc.essentialcommands.text.TextFormatType;
import com.fibermc.essentialcommands.types.AllowTeleportRequests;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

public final class TeleportToggleCommand {
    private TeleportToggleCommand() {}

    public static int toggle(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        var profile = PlayerProfile.accessFromContextOrThrow(context);
        var newValue = profile.getAllowTeleportRequests() == AllowTeleportRequests.ON
            ? AllowTeleportRequests.OFF
            : AllowTeleportRequests.ON;
        return set(context, newValue);
    }

    public static Command<CommandSourceStack> setTo(AllowTeleportRequests value) {
        return (context) -> set(context, value);
    }

    private static int set(CommandContext<CommandSourceStack> context, AllowTeleportRequests value)
        throws CommandSyntaxException
    {
        var player = context.getSource().getPlayerOrException();
        var profile = PlayerProfile.access(player);
        profile.setAllowTeleportRequests(value);
        profile.setDirty();
        profile.save(context.getSource().getServer().registryAccess());

        PlayerData.access(player).sendCommandFeedback("cmd.tptoggle.feedback." + value.getId());
        return Command.SINGLE_SUCCESS;
    }

    /**
     * Auto-denies a tpa/tpahere request if the target does not allow teleport requests, notifying the sender (and,
     * unless silent, the target).
     *
     * @return true if the request was denied and should not be sent.
     */
    static boolean denyIfDisallowed(ServerPlayer senderPlayer, ServerPlayer targetPlayer) {
        var targetPlayerProfile = PlayerProfile.access(targetPlayer);
        var senderPlayerProfile = PlayerProfile.access(senderPlayer);
        var targetPlayerText = targetPlayer.getDisplayName().copy()
            .withStyle(senderPlayerProfile.getStyle(TextFormatType.Accent));

        switch (targetPlayerProfile.getAllowTeleportRequests()) {
            case ON -> {
                return false;
            }
            case OFF -> {
                PlayerData.access(targetPlayer).sendMessage(
                    "cmd.tpask.receive.disallowed",
                    senderPlayer.getDisplayName().copy().withStyle(targetPlayerProfile.getStyle(TextFormatType.Accent)));
                PlayerData.access(senderPlayer).sendCommandError("cmd.tpask.error.disallowed", targetPlayerText);
                return true;
            }
            case OFF_SILENT -> {
                PlayerData.access(senderPlayer).sendCommandError("cmd.tpask.error.disallowed_silent", targetPlayerText);
                return true;
            }
        }
        throw new IllegalStateException();
    }
}
