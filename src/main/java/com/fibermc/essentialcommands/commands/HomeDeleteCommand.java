package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.ECText;
import com.fibermc.essentialcommands.PlayerData;
import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.util.TextUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;


public class HomeDeleteCommand implements Command<ServerCommandSource> {

    public HomeDeleteCommand() {}

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        int out = 1;
        ServerCommandSource source = context.getSource();
        //Store command sender
        ServerPlayerEntity senderPlayer = source.getPlayer();
        PlayerData senderPlayerData = ((ServerPlayerEntityAccess)senderPlayer).getEcPlayerData();
        //Store home name
        String homeName = StringArgumentType.getString(context, "home_name");

        //Remove Home - TODO Require player to type the command again to confirm deletion.
        boolean wasSuccessful = senderPlayerData.removeHome(homeName);

        //inform command sender that the home has been removed
        if (wasSuccessful) {
            source.sendFeedback(
                TextUtil.concat(
                    ECText.getInstance().getText("cmd.home.feedback.1").setStyle(CONFIG.FORMATTING_DEFAULT.getValue()),
                    new LiteralText(homeName).setStyle(CONFIG.FORMATTING_ACCENT.getValue()),
                    ECText.getInstance().getText("cmd.home.delete.feedback.2").setStyle(CONFIG.FORMATTING_DEFAULT.getValue())
                ), CONFIG.BROADCAST_TO_OPS.getValue());
        } else {
            source.sendError(TextUtil.concat(
                ECText.getInstance().getText("cmd.home.feedback.1").setStyle(CONFIG.FORMATTING_ERROR.getValue()),
                new LiteralText(homeName).setStyle(CONFIG.FORMATTING_ACCENT.getValue()),
                ECText.getInstance().getText("cmd.home.delete.error.2").setStyle(CONFIG.FORMATTING_ERROR.getValue())
            ));
            out = 0;
        }

        return out;
    }
}
