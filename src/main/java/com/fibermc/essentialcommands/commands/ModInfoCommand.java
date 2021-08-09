package com.fibermc.essentialcommands.commands;

import com.fibermc.essentialcommands.EssentialCommands;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.jpcode.eccore.util.TextUtil;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;

public class ModInfoCommand implements Command<ServerCommandSource> {

    private final String modVersion = EssentialCommands.MOD_METADATA.getVersion().getFriendlyString();

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {

        context.getSource().sendFeedback(TextUtil.concat(
            new LiteralText(EssentialCommands.MOD_METADATA.getName()).setStyle(CONFIG.FORMATTING_DEFAULT.getValue()),
            new LiteralText(" "),
            new LiteralText(modVersion).setStyle(CONFIG.FORMATTING_ACCENT.getValue())
        ), false);

        return 0;
    }
}
