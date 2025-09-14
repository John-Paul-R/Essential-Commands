package com.fibermc.essentialcommands.database;

import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import net.minecraft.util.Util;

public class DatabaseHelper {
    private static final Executor EXECUTOR = Util.getIoWorkerExecutor(); //Executors.newVirtualThreadPerTaskExecutor();

    // Simple version - wraps SQLException in RuntimeException
    public static <T> CompletableFuture<T> async(SqlSupplier<T> supplier) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return supplier.get();
            } catch (SQLException e) {
                throw new RuntimeException("Database error", e);
            }
        }, EXECUTOR);
    }

    // Version with custom exception handling
    public static <T> CompletableFuture<T> async(SqlSupplier<T> supplier, T defaultValue) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return supplier.get();
            } catch (SQLException e) {
                // Log error here if needed
                return defaultValue;
            }
        }, EXECUTOR);
    }

    // Version that completes exceptionally on SQL errors
    public static <T> CompletableFuture<T> asyncWithSqlException(SqlSupplier<T> supplier) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return supplier.get();
            } catch (SQLException e) {
                throw new RuntimeException(e); // Will cause CompletableFuture to complete exceptionally
            }
        }, EXECUTOR);
    }

    @FunctionalInterface
    public interface SqlSupplier<T> {
        T get() throws SQLException;
    }
}
