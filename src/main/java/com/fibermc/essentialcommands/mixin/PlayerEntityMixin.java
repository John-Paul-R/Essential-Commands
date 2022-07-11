package com.fibermc.essentialcommands.mixin;

import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
    @Inject(method = "getDisplayName", at = @At("RETURN"), cancellable = true)
    public void onGetDisplayName(CallbackInfoReturnable<Text> cir) {

    }

    @ModifyVariable(
        method = "getDisplayName",
        at = @At("STORE"),
        ordinal = 0)
    public MutableText injected(MutableText teamDecoratedName) {
        if (!ServerPlayerEntity.class.isAssignableFrom(this.getClass())) {
            // I *think* this check is correct, but frankly am not sure.
            // Reflection & Mixins hurt my brain.
            return teamDecoratedName;
        }
        try {
            var playerData = ((ServerPlayerEntityAccess)(Object)this).getEcPlayerData();
            if (playerData.getNickname().isPresent()) {
                MutableText nickname = playerData.getFullNickname();
                // Re-add "whisper" click event unless the nickname has a click event set.
                Style nicknameStyle = nickname.getStyle();
                if (nicknameStyle.getClickEvent() == null) {
                    nickname.setStyle(nicknameStyle.withClickEvent(teamDecoratedName.getStyle().getClickEvent()));
                }
                // Send nickname (styled appropriately for player team) as return value for getDisplayName().
                ServerPlayerEntity serverPlayerEntity = ((ServerPlayerEntity)(Object)this);
                return Team.decorateName(
                    serverPlayerEntity.getScoreboard().getPlayerTeam(serverPlayerEntity.getEntityName()),
                    nickname
                );
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return teamDecoratedName;
    }
}
