package com.fibermc.essentialcommands.mixin;

import java.util.List;

import com.fibermc.essentialcommands.access.EntitySelectorNicknameAccess;
import com.fibermc.essentialcommands.commands.NicknameTargetResolver;
import com.fibermc.essentialcommands.types.NicknameCommandArgMode;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import static com.fibermc.essentialcommands.EssentialCommands.CONFIG;

/**
 * Adds nickname fallback after vanilla EntitySelector resolution when global
 * nickname command arguments are enabled.
 */
@Mixin(EntitySelector.class)
public abstract class EntitySelectorNicknameMixin implements EntitySelectorNicknameAccess {
    @Shadow
    @Final
    private String playerName;

    @Override
    public String ec$getPlayerName() {
        return this.playerName;
    }

    @Inject(method = "findPlayers", at = @At("RETURN"), cancellable = true)
    private void ec$resolveNicknameForPlayers(
        CommandSourceStack source,
        CallbackInfoReturnable<List<ServerPlayer>> cir
    ) {
        if (
            CONFIG.NICKNAMES_AS_COMMAND_ARG != NicknameCommandArgMode.Everywhere
            || !cir.getReturnValue().isEmpty()
        ) {
            return;
        }

        ServerPlayer player = NicknameTargetResolver.resolvePlayerByNickname(this.playerName);
        if (player != null) {
            cir.setReturnValue(List.of(player));
        }
    }

    @Inject(method = "findEntities", at = @At("RETURN"), cancellable = true)
    private void ec$resolveNicknameForEntities(
        CommandSourceStack source,
        CallbackInfoReturnable<List<? extends Entity>> cir
    ) {
        if (
            CONFIG.NICKNAMES_AS_COMMAND_ARG != NicknameCommandArgMode.Everywhere
            || !cir.getReturnValue().isEmpty()
        ) {
            return;
        }

        ServerPlayer player = NicknameTargetResolver.resolvePlayerByNickname(this.playerName);
        if (player != null) {
            cir.setReturnValue(List.of(player));
        }
    }
}
