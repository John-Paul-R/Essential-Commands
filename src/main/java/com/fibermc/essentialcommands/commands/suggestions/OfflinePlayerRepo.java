package com.fibermc.essentialcommands.commands.suggestions;

import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.response.NameAndId;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;

public class OfflinePlayerRepo {

    private final HashMap<String, NameAndId> gameProfileCache = new HashMap<>();
    private final MinecraftServer server;

    public OfflinePlayerRepo(MinecraftServer server) {
        this.server = server;
    }

    public CompletableFuture<ServerPlayer> getOfflinePlayerByNameAsync(String playerName) {
        return getGameProfile(playerName)
            .handle(((gameProfile, throwable) -> gameProfile.map(this::getOfflinePlayer).orElse(null)));
    }

    public ServerPlayer getOfflinePlayer(NameAndId playerProfile) {
        var player = new ServerPlayer(
            server,
            server.overworld(),
            new GameProfile(playerProfile.id(), playerProfile.name()),
            ClientInformation.createDefault());

        server.getPlayerList().loadPlayerData(new net.minecraft.server.players.NameAndId(playerProfile.id(), playerProfile.name()));

        return player;
    }

    public CompletableFuture<Optional<NameAndId>> getGameProfile(String playerName) {
        var profile = gameProfileCache.get(playerName);
        if (profile != null) {
            CompletableFuture.completedFuture(profile);
        }
        return requestGameProfile(playerName)
            .whenComplete((gameProfile, err) -> {
                gameProfile.ifPresent(nameAndId -> gameProfileCache.put(nameAndId.name(), nameAndId));
            });
    }

    private final ExecutorService gameProfileExecutor = Executors.newVirtualThreadPerTaskExecutor();

    private CompletableFuture<Optional<NameAndId>> requestGameProfile(String playerName) {
        CompletableFuture<Optional<NameAndId>> completable = new CompletableFuture<>();

        gameProfileExecutor.execute(() -> {
            try {
                completable.complete(server.services().profileRepository().findProfileByName(playerName));
            } catch (Exception ex) {
                completable.completeExceptionally(ex);
            }
        });

        return completable;
    }
}
