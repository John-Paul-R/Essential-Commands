package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.teleportation.PlayerTeleporter;
import com.fibermc.essentialcommands.text.ECText;
import com.fibermc.essentialcommands.text.TextFormatType;
import com.fibermc.essentialcommands.types.MinecraftLocation;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.CommandException;
import net.minecraft.server.command.ServerCommandSource;

public class BedCommand implements Command<ServerCommandSource> {
    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var player = context.getSource().getPlayer();
        var spawnPos = player.getSpawnPointPosition();
        var spawnDim = player.getSpawnPointDimension();

        if (spawnPos == null) {
            throw new CommandException(ECText.access(player).getText("cmd.bed.error.none_set", TextFormatType.Error));
        }
        PlayerTeleporter.requestTeleport(
            player,
            new MinecraftLocation(spawnDim, spawnPos.getX(), spawnPos.getY(), spawnPos.getZ()),
            ECText.access(player).getText("cmd.bed.bed_destination_name", TextFormatType.Accent));

        return 0;
    }
}
