package com.fibermc.essentialcommands.commands.suggestions;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.ProfileLookupCallback;

import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

public class OfflinePlayerRepo {

    private final HashMap<String, GameProfile> gameProfileCache = new HashMap<>();
    private final MinecraftServer server;

    public OfflinePlayerRepo(MinecraftServer server) {
        this.server = server;
    }

    public CompletableFuture<ServerPlayerEntity> getOfflinePlayerByNameAsync(String playerName) {
        return getGameProfile(playerName)
            .handle(((gameProfile, throwable) -> gameProfile == null
                ? null
                : getOfflinePlayer(gameProfile)));
    }

    public ServerPlayerEntity getOfflinePlayer(GameProfile playerProfile) {
        var player = new ServerPlayerEntity(
            server,
            server.getOverworld(),
            playerProfile,
            SyncedClientOptions.createDefault());

        server.getPlayerManager().loadPlayerData(player);

        return player;
    }

    public CompletableFuture<GameProfile> getGameProfile(String playerName) {
        var profile = gameProfileCache.get(playerName);
        if (profile != null) {
            CompletableFuture.completedFuture(profile);
        }
        return requestGameProfile(playerName)
            .whenComplete((gameProfile, err) -> {
                if (gameProfile != null) {
                    gameProfileCache.put(gameProfile.getName(), gameProfile);
                }
            });
    }

    private CompletableFuture<GameProfile> requestGameProfile(String playerName) {
        CompletableFuture<GameProfile> out = new CompletableFuture<>();
        server.getGameProfileRepo().findProfilesByNames(
            new String[]{playerName},
            new ProfileLookupCallback() {
                @Override
                public void onProfileLookupSucceeded(GameProfile profile) {
                    System.out.println(profile.toString());
                    out.complete(profile);
                }

                @Override
                public void onProfileLookupFailed(String profileName, Exception exception) {
                    out.complete(null);
                }
            });
        return out;
    }
}
