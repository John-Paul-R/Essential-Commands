package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.ECText;
import com.fibermc.essentialcommands.PlayerData;
import com.fibermc.essentialcommands.TextFormatType;
import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.types.MinecraftLocation;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;


public class HomeSetCommand implements Command<ServerCommandSource> {

    public HomeSetCommand() {}

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        //Store command sender
        ServerPlayerEntity senderPlayer = source.getPlayer();
        //Store home name
        String homeName = StringArgumentType.getString(context, "home_name");

        //Add home to PlayerData
        //TODO if home with given name is already set, warn of overwrite and require that the command be typed again, or a confirmation message be given
        PlayerData pData = ((ServerPlayerEntityAccess)senderPlayer).getEcPlayerData();
        var homeNameText = ECText.accent(homeName);
        int successCode = 0;
        try {
             successCode = pData.addHome(homeName, new MinecraftLocation(senderPlayer));
        } catch (CommandSyntaxException e) {
            source.sendError(
                ECText.getInstance().getText("cmd.home.set.error.exists", TextFormatType.Error, homeNameText)
            );
        }

        pData.markDirty();
        pData.save();
        //inform command sender that the home has been set
        if (successCode == 1) {
            source.sendFeedback(
                ECText.getInstance().getText("cmd.home.set.feedback", homeNameText),
                CONFIG.BROADCAST_TO_OPS
            );
        }

        return successCode;
    }
}
