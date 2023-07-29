package com.fibermc.essentialcommands.commands;

import java.io.IOException;
import java.nio.file.Path;

import com.fibermc.essentialcommands.EssentialCommands;
import com.fibermc.essentialcommands.playerdata.PlayerData;
import com.fibermc.essentialcommands.util.FileUtil;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import dev.jpcode.eccore.util.TextUtil;

public final class RulesCommand {

    private RulesCommand() {}

    private static Text rulesText;

    public static int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        context.getSource().sendFeedback(() -> rulesText, false);
        return 0;
    }

    public static int reloadCommand(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var playerData = PlayerData.accessFromContextOrThrow(context);

        try {
            reload(context.getSource().getServer());
            playerData.sendCommandFeedback("rules.reload.success");
        } catch (IOException e) {
            playerData.sendCommandError("rules.reload.error.unexpected");
            e.printStackTrace();
        }
        return 0;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void reload(MinecraftServer server) throws IOException {
        Path mcDir = server.getRunDirectory().toPath();
        var rulesFile = mcDir.resolve("config/essentialcommands/rules.txt").toFile();
        rulesFile.getParentFile().mkdirs();
        if (rulesFile.createNewFile()) {
            EssentialCommands.LOGGER.info("Created rules file at path: " + rulesFile.toPath());
        }
        rulesText = TextUtil.parseText(FileUtil.readString(rulesFile.toPath()));
    }
}
