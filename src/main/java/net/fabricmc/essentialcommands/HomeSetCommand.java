package net.fabricmc.essentialcommands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.network.MessageType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

public class HomeSetCommand implements Command<ServerCommandSource> {

    private PlayerDataManager dataManager;
    public HomeSetCommand(PlayerDataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        
        //Store command sender
        ServerPlayerEntity senderPlayer = context.getSource().getPlayer();
        //Store home name
        String homeName = StringArgumentType.getString(context, "home_name");
        
        //Add home to PlayerData
        PlayerData pData = dataManager.getOrCreate(senderPlayer);
        pData.addHome(homeName, new MinecraftLocation(senderPlayer));
        pData.markDirty();
        dataManager.savePlayerData(senderPlayer);
        //inform command sender that the home has been set
        senderPlayer.sendChatMessage(
                new LiteralText("Home '").formatted(Formatting.GOLD)
                    .append(new LiteralText(homeName).formatted(Formatting.LIGHT_PURPLE))
                    .append(new LiteralText("' set.").formatted(Formatting.GOLD))
            , MessageType.SYSTEM);
        
        return 1;
    }
}
