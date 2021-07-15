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


public class HomeCommand implements Command<ServerCommandSource> {

    public HomeCommand() {
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        int out;
        //Store command sender
        ServerPlayerEntity senderPlayer = context.getSource().getPlayer();
        PlayerData senderPlayerData = ((ServerPlayerEntityAccess)senderPlayer).getEcPlayerData();
        //Store home name
        String homeName = StringArgumentType.getString(context, "home_name");

        //Get home location
        MinecraftLocation loc = senderPlayerData.getHomeLocation(homeName);

        // Teleport & chat message
        if (loc != null) {
            //Teleport player to home location
            PlayerTeleporter.requestTeleport(senderPlayerData, loc, ECText.getInstance().getText("cmd.home.location_name", homeName));
            out = 1;
        } else {
//            senderPlayer.sendSystemMessage(
//                    new LiteralText("No home with the name '").setStyle(Config.FORMATTING_ERROR)
//                            .append(new LiteralText(homeName).setStyle(Config.FORMATTING_ACCENT))
//                            .append(new LiteralText("' could be found.").setStyle(Config.FORMATTING_ERROR))
//                    , new UUID(0, 0));
            Message msg = ECText.getInstance().getText("cmd.home.tp.error.not_found", homeName);
            throw new CommandSyntaxException(new SimpleCommandExceptionType(msg), msg);


        }


        return out;
    }

}
