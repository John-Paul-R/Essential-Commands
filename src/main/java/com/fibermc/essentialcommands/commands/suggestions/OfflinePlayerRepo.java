package com.fibermc.essentialcommands.commands.suggestions;

import com.mojang.authlib.Agent;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.ProfileLookupCallback;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class OfflinePlayerRepo {

    private final HashMap<String, GameProfile> _gameProfileCache = new HashMap<>();
    private final MinecraftServer _server;
    public OfflinePlayerRepo(MinecraftServer server) {
        _server = server;
    }

    public CompletableFuture<ServerPlayerEntity> getOfflinePlayerByNameAsync(String playerName) {
        return getGameProfile(playerName)
            .handle(((gameProfile, throwable) -> gameProfile == null
                ? null
                : getOfflinePlayer(gameProfile)));
    }

    public ServerPlayerEntity getOfflinePlayer(GameProfile playerProfile) {
        var player = new ServerPlayerEntity(
            _server,
            _server.getOverworld(),
            playerProfile,
            null);

        _server.getPlayerManager().loadPlayerData(player);

        return player;
    }

    public CompletableFuture<GameProfile> getGameProfile(String playerName) {
        var profile = _gameProfileCache.get(playerName);
        if (profile != null) {
            CompletableFuture.completedFuture(profile);
        }
        return requestGameProfile(playerName)
            .whenComplete((gameProfile, err) -> {
                if (gameProfile != null) {
                    _gameProfileCache.put(gameProfile.getName(), gameProfile);
                }
        });
    }

    private CompletableFuture<GameProfile> requestGameProfile(String playerName) {
        CompletableFuture<GameProfile> out = new CompletableFuture<>();
        _server.getGameProfileRepo().findProfilesByNames(
            new String[]{playerName},
            Agent.MINECRAFT,
            new ProfileLookupCallback() {
                @Override
                public void onProfileLookupSucceeded(GameProfile profile) {
                    System.out.println(profile.toString());
                    out.complete(profile);
                }

                @Override
                public void onProfileLookupFailed(GameProfile profile, Exception exception) {
                    out.complete(null);
                }
            });
        return out;
    }
}
