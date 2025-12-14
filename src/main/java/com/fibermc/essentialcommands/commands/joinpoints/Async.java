package com.fibermc.essentialcommands.commands.joinpoints;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.fibermc.essentialcommands.EssentialCommands;
import com.fibermc.essentialcommands.text.ECText;
import com.fibermc.essentialcommands.text.TextFormatType;

import net.minecraft.text.Text;
import net.minecraft.util.Util;

final class Async {
    private Async() {}

    public static <T> CompletableFuture<T> runCommand(Supplier<T> supplier, Consumer<Function<ECText, Text>> sendError)
    {
        return CompletableFuture
            .supplyAsync(supplier, Executors.newVirtualThreadPerTaskExecutor())
            .exceptionallyAsync(threadException -> {
                if (!(threadException instanceof CompletionException)) {
                    EssentialCommands.LOGGER.error(threadException);
                    sendError.accept(ecText -> ecText.getText(
                        "cmd.joinpoint.error.unknown",
                        TextFormatType.Error,
                        Text.literal(threadException.getMessage())
                    ));
                    return null;
                }
                Function<ECText, Text> errorFunction = switch (threadException.getCause()) {
                    case JoinpointException e -> e::message;
                    default -> {
                        EssentialCommands.LOGGER.error("Unknown error in a Joinpoint list command", threadException);
                        yield ecText -> ecText.getText(
                            "cmd.joinpoint.error.unknown",
                            TextFormatType.Error,
                            Text.literal(threadException.getCause().getMessage())
                        );
                    }
                };
                sendError.accept(errorFunction);
                return null;
            }, Util.getMainWorkerExecutor());
    }
}
