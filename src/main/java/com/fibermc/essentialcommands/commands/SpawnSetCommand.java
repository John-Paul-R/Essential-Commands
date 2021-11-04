package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.ECText;
import com.fibermc.essentialcommands.ManagerLocator;
import com.fibermc.essentialcommands.WorldDataManager;
import com.fibermc.essentialcommands.types.MinecraftLocation;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;


public class SpawnSetCommand implements Command<ServerCommandSource> {

    public SpawnSetCommand() {}

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        WorldDataManager worldDataManager = ManagerLocator.getInstance().getWorldDataManager();

        ServerCommandSource source = context.getSource();
        //Store command sender
        ServerPlayerEntity senderPlayer = source.getPlayer();

        int successCode = 1;

        //Set spawn
        MinecraftLocation loc = new MinecraftLocation(senderPlayer);
        worldDataManager.setSpawn(loc);

        //inform command sender that the home has been set
        source.sendFeedback(
            ECText.getInstance().getText("cmd.spawn.set.feedback").setStyle(CONFIG.FORMATTING_DEFAULT.getValue())
                .append(loc.toLiteralTextSimple().setStyle(CONFIG.FORMATTING_ACCENT.getValue()))
            , CONFIG.BROADCAST_TO_OPS.getValue());

        return successCode;
    }
}
