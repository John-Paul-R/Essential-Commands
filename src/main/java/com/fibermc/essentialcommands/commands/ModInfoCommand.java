package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.EssentialCommands;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.jpcode.eccore.util.TextUtil;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;

public class ModInfoCommand implements Command<ServerCommandSource> {

    private final String modVersion = EssentialCommands.MOD_METADATA.getVersion().getFriendlyString();

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {

        context.getSource().sendFeedback(TextUtil.concat(
            Text.literal(EssentialCommands.MOD_METADATA.getName()).setStyle(CONFIG.FORMATTING_DEFAULT),
            Text.literal(" "),
            Text.literal(modVersion).setStyle(CONFIG.FORMATTING_ACCENT)
        ), false);

        return 0;
    }
}
