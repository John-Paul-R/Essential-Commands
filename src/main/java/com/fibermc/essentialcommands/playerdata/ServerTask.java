package com.fibermc.essentialcommands.playerdata;

import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import net.minecraft.server.MinecraftServer;

public interface ServerTask {
    // treat null as != null always
    @Nullable String id();

    Consumer<MinecraftServer> task();

    static ServerTask of(@Nullable String id, Runnable runnable) {
        return new ServerTask() {
            @Override
            public @Nullable String id() {
                return null;
            }

            @Override
            public Consumer<MinecraftServer> task() {
                return ignServer -> runnable.run();
            }
        };
    }

    static ServerTask of(@Nullable String id, Consumer<MinecraftServer> task) {
        return new ServerTask() {
            @Override
            public @Nullable String id() {
                return id;
            }

            @Override
            public Consumer<MinecraftServer> task() {
                return task;
            }
        };
    }
}
