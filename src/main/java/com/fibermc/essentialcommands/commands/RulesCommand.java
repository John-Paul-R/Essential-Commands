package com.fibermc.essentialcommands.commands;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.fibermc.essentialcommands.EssentialCommands;
import com.fibermc.essentialcommands.playerdata.PlayerData;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;

import dev.jpcode.eccore.util.TextUtil;

public final class RulesCommand {

    private RulesCommand() {}

    private static Component rulesText;

    public static int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        context.getSource().sendSuccess(() -> rulesText, false);
        return 0;
    }

    public static int reloadCommand(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
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
        Path mcDir = server.getServerDirectory();
        var rulesFile = mcDir.resolve("config/essentialcommands/rules.txt").toFile();
        rulesFile.getParentFile().mkdirs();
        if (rulesFile.createNewFile()) {
            EssentialCommands.LOGGER.info("Created rules file at path: " + rulesFile.toPath());
        }
        String rulesStr = String.join("\n", Files.readAllLines(rulesFile.toPath()));
        rulesText = TextUtil.parseText(rulesStr);
    }
}
