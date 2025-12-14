package com.fibermc.essentialcommands.mixin;

import com.fibermc.essentialcommands.playerdata.PlayerDataManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.scores.PlayerTeam;

@Mixin(ServerScoreboard.class)
public class ServerScoreboardMixin {

    @Inject(method = "addPlayerToTeam", at = @At("RETURN"))
    public void onAddPlayerToTeam(String playerName, PlayerTeam team, CallbackInfoReturnable<Boolean> cir) {
        try {
            PlayerDataManager.getInstance().markNicknameDirty(playerName);
        } catch (NullPointerException ignore) {
            // ign
        }
    }

    @Inject(method = "removePlayerFromTeam(Ljava/lang/String;Lnet/minecraft/world/scores/PlayerTeam;)V", at = @At("RETURN"))
    public void onRemovePlayerFromTeam(String playerName, PlayerTeam team, CallbackInfo ci) {
        try {
            PlayerDataManager.getInstance().markNicknameDirty(playerName);
        } catch (NullPointerException ignore) {
            // ign
        }
    }

    @Inject(method = "onTeamChanged", at = @At("RETURN"))
    public void onUpdateScoreboardTeam(PlayerTeam team, CallbackInfo ci) {
        var playerDataManager = PlayerDataManager.getInstance();
        if (playerDataManager != null) {
            team.getPlayers().forEach(playerDataManager::markNicknameDirty);
        }
    }

    @Inject(method = "onTeamRemoved", at = @At("RETURN"))
    public void onUpdateRemovedTeam(PlayerTeam team, CallbackInfo ci) {
        var playerDataManager = PlayerDataManager.getInstance();
        if (playerDataManager != null) {
            team.getPlayers().forEach(playerDataManager::markNicknameDirty);
        }
    }

}
