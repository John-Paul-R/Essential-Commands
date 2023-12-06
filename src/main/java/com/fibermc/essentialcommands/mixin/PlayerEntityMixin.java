package com.fibermc.essentialcommands.mixin;

import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.playerdata.PlayerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
    @Inject(method = "getDisplayName", at = @At("RETURN"))
    public void onGetDisplayName(CallbackInfoReturnable<Text> cir) {

    }

    @ModifyVariable(
        method = "getDisplayName",
        at = @At("STORE"))
    // these are just IDE errors, it works in game
    public MutableText injected(MutableText teamDecoratedName) {
        // Verify that this is a ServerPlayerEntity instance.
        if (!ServerPlayerEntity.class.isAssignableFrom(this.getClass())) {
            // I *think* this check is correct, but frankly am not sure.
            // Reflection & Mixins hurt my brain.
            return teamDecoratedName;
        }

        var playerData = ((ServerPlayerEntityAccess) this).ec$getPlayerData();
        var name = getNicknameStyledName(teamDecoratedName, playerData);

        return playerData.isAfk()
            ? Text.empty()
                .append(CONFIG.AFK_PREFIX)
                .append(name)
            : name;
    }

    private static MutableText getNicknameStyledName(MutableText teamDecoratedName, PlayerData playerData) {
        try {
            if (playerData.getNickname().isPresent()) {
                MutableText nickname = playerData.getFullNickname();
                // Re-add "whisper" click event unless the nickname has a click event set.
                Style nicknameStyle = nickname.getStyle();
                if (nicknameStyle.getClickEvent() == null) {
                    nickname.setStyle(nicknameStyle.withClickEvent(teamDecoratedName.getStyle().getClickEvent()));
                }
                // Send nickname (styled appropriately for player team) as return value for getDisplayName().
                ServerPlayerEntity serverPlayerEntity = playerData.getPlayer();
                return Team.decorateName(
                    serverPlayerEntity.getScoreboardTeam(),
                    nickname
                );
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return teamDecoratedName;
    }
}
