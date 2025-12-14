package com.fibermc.essentialcommands.mixin;

import com.fibermc.essentialcommands.access.ServerPlayerEntityAccess;
import com.fibermc.essentialcommands.playerdata.PlayerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.PlayerTeam;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntityMixin {
    @Inject(method = "getDisplayName", at = @At("RETURN"))
    public void onGetDisplayName(CallbackInfoReturnable<Component> cir) {

    }

    @ModifyVariable(
        method = "getDisplayName",
        at = @At("STORE"))
    // these are just IDE errors, it works in game
    public MutableComponent injected(MutableComponent teamDecoratedName) {
        // Verify that this is a ServerPlayerEntity instance.
        if (!ServerPlayer.class.isAssignableFrom(this.getClass())) {
            // I *think* this check is correct, but frankly am not sure.
            // Reflection & Mixins hurt my brain.
            return teamDecoratedName;
        }

        var playerData = ((ServerPlayerEntityAccess) this).ec$getPlayerData();
        var name = getNicknameStyledName(teamDecoratedName, playerData);

        return playerData.isAfk()
            ? Component.empty()
                .append(CONFIG.AFK_PREFIX)
                .append(name)
            : name;
    }

    private static MutableComponent getNicknameStyledName(MutableComponent teamDecoratedName, PlayerData playerData) {
        try {
            if (playerData.getNickname().isPresent()) {
                MutableComponent nickname = playerData.getFullNickname();
                // Re-add "whisper" click event unless the nickname has a click event set.
                Style nicknameStyle = nickname.getStyle();
                if (nicknameStyle.getClickEvent() == null) {
                    nickname.setStyle(nicknameStyle.withClickEvent(teamDecoratedName.getStyle().getClickEvent()));
                }
                // Send nickname (styled appropriately for player team) as return value for getDisplayName().
                ServerPlayer serverPlayerEntity = playerData.getPlayer();
                return PlayerTeam.formatNameForTeam(
                    serverPlayerEntity.getTeam(),
                    nickname
                );
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return teamDecoratedName;
    }
}
