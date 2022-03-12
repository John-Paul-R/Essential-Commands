package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.ECText;
import com.fibermc.essentialcommands.ManagerLocator;
import com.fibermc.essentialcommands.PlayerTeleporter;
import com.fibermc.essentialcommands.WorldDataManager;
import com.fibermc.essentialcommands.types.MinecraftLocation;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;

public class SpawnCommand implements Command<ServerCommandSource> {

    public SpawnCommand() {}

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        WorldDataManager worldDataManager = ManagerLocator.getInstance().getWorldDataManager();
        MinecraftLocation loc = worldDataManager.getSpawn();

        if (loc == null) {
            context.getSource().sendError(
                ECText.getInstance().getText("cmd.spawn.tp.error.no_spawn_set").setStyle(CONFIG.FORMATTING_ERROR.getValue()));
            return -2;
        }

        ServerPlayerEntity senderPlayer = context.getSource().getPlayer();

        // Teleport & chat message
        PlayerTeleporter.requestTeleport(
            senderPlayer,
            loc,
            ECText.getInstance().getText("cmd.spawn.location_name"));
        return 1;
    }

}
