package com.fibermc.essentialcommands.mixin;

import com.fibermc.essentialcommands.playerdata.PlayerDataManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.scoreboard.Team;

@Mixin(ServerScoreboard.class)
public class ServerScoreboardMixin {

    @Inject(method = "addScoreHolderToTeam", at = @At("RETURN"))
    public void onAddPlayerToTeam(String playerName, Team team, CallbackInfoReturnable<Boolean> cir) {
        try {
            PlayerDataManager.getInstance().markNicknameDirty(playerName);
        } catch (NullPointerException ignore) {
            // ign
        }
    }

    @Inject(method = "removeScoreHolderFromTeam", at = @At("RETURN"))
    public void onRemovePlayerFromTeam(String playerName, Team team, CallbackInfo ci) {
        try {
            PlayerDataManager.getInstance().markNicknameDirty(playerName);
        } catch (NullPointerException ignore) {
            // ign
        }
    }

    @Inject(method = "updateScoreboardTeam", at = @At("RETURN"))
    public void onUpdateScoreboardTeam(Team team, CallbackInfo ci) {
        var playerDataManager = PlayerDataManager.getInstance();
        if (playerDataManager != null) {
            team.getPlayerList().forEach(playerDataManager::markNicknameDirty);
        }
    }

    @Inject(method = "updateRemovedTeam", at = @At("RETURN"))
    public void onUpdateRemovedTeam(Team team, CallbackInfo ci) {
        var playerDataManager = PlayerDataManager.getInstance();
        if (playerDataManager != null) {
            team.getPlayerList().forEach(playerDataManager::markNicknameDirty);
        }
    }

}
