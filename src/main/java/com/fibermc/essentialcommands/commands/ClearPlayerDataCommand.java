package com.fibermc.essentialcommands.commands;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

import com.fibermc.essentialcommands.playerdata.PlayerDataFactory;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;

public class ClearPlayerDataCommand implements Command<ServerCommandSource> {
    // divide by 2 to prevent overflow...
    static final int INITIAL_LAST_EXEC_TIME = Integer.MIN_VALUE / 2;
    static final int MAX_SECONDS_FOR_CONFIRM = 30;
    static final HashMap<UUID, Integer> LAST_EXEC_TIME_PER_PLAYER_MAP = new HashMap<>();
    static int lastConsoleExecTime = INITIAL_LAST_EXEC_TIME;

    @Override
    public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        var source = context.getSource();
        if (!CONFIG.ENABLE_DELETE_ALL_PLAYER_DATA) {
            source.sendError(Text.literal("This command is not enabled"));
            return -1;
        }

        var server = source.getServer();

        int currentTicks = server.getTicks();
        int lastExecTime;
        if (source.isExecutedByPlayer()) {
            var playerId = source.getPlayerOrThrow().getUuid();
            lastExecTime = LAST_EXEC_TIME_PER_PLAYER_MAP.getOrDefault(playerId, INITIAL_LAST_EXEC_TIME);
            LAST_EXEC_TIME_PER_PLAYER_MAP.put(playerId, currentTicks);
        } else {
            lastExecTime = lastConsoleExecTime;
            lastConsoleExecTime = currentTicks;
        }

        if (currentTicks - lastExecTime > 20 * MAX_SECONDS_FOR_CONFIRM) {
            source.sendFeedback(() -> Text.literal(
                "Are you sure you want to disconnect all players and permanently delete ALL"
                    + " Essential Commands player data?"
                    + " This action is irreversible. Run the command again to confirm."),
                true);
            return 0;
        }

        server.getPlayerManager().disconnectAllPlayers();

        try {
            var playerDataDirPath = PlayerDataFactory.getPlayerDataDirectoryPath(server);
            var files = playerDataDirPath.toFile().listFiles();

            assert files != null;

            Arrays.stream(files)
                .forEach(File::delete);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return SINGLE_SUCCESS;
    }
}
