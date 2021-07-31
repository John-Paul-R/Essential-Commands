package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.ECText;
import com.fibermc.essentialcommands.PlayerData;
import com.fibermc.essentialcommands.PlayerTeleporter;
import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.types.MinecraftLocation;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Set;


public class HomeCommand implements Command<ServerCommandSource> {

    public HomeCommand() {
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        //Store command sender
        ServerPlayerEntity senderPlayer = context.getSource().getPlayer();
        PlayerData senderPlayerData = ((ServerPlayerEntityAccess)senderPlayer).getEcPlayerData();
        //Store home name
        String homeName = StringArgumentType.getString(context, "home_name");

        return exec(senderPlayerData, homeName);
    }

    public int runDefault(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        PlayerData playerData = ((ServerPlayerEntityAccess) context.getSource().getPlayer()).getEcPlayerData();
        Set<String> homeNames = playerData.getHomeNames();
        if (homeNames.size() > 1) {
            throw CommandUtil.createSimpleException(ECText.getInstance().getText("cmd.home.tp.error.shortcut_more_than_one"));
        } else if (homeNames.size() < 1) {
            throw CommandUtil.createSimpleException(ECText.getInstance().getText("cmd.home.tp.error.shortcut_none_exist"));
        }

        return exec(
                playerData,
                homeNames.stream().findAny().get()
        );
    }

    private int exec(PlayerData senderPlayerData, String homeName) throws CommandSyntaxException {
        int out;

        //Get home location
        MinecraftLocation loc = senderPlayerData.getHomeLocation(homeName);

        // Teleport & chat message
        if (loc != null) {
            //Teleport player to home location
            PlayerTeleporter.requestTeleport(senderPlayerData, loc, ECText.getInstance().getText("cmd.home.location_name", homeName));
            out = 1;
        } else {
            Message msg = ECText.getInstance().getText("cmd.home.tp.error.not_found", "null");
            throw new CommandSyntaxException(new SimpleCommandExceptionType(msg), msg);
        }

        return out;
    }
}
