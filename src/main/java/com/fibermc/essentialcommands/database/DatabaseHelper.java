package com.fibermc.essentialcommands.database;

import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import com.fibermc.essentialcommands.EssentialCommands;

import net.minecraft.util.Util;

public final class DatabaseHelper {
    private DatabaseHelper() {}

    private static final Executor EXECUTOR = Util.getIoWorkerExecutor(); // Executors.newVirtualThreadPerTaskExecutor();

    public static <T> CompletableFuture<T> async(SqlSupplier<T> supplier) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return supplier.get();
            } catch (SQLException e) {
                EssentialCommands.LOGGER.error("Database error", e);
                throw new RuntimeException("Database error", e);
            }
        }, EXECUTOR);
    }

    @FunctionalInterface
    public interface SqlSupplier<T> {
        T get() throws SQLException;
    }
}
