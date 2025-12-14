package com.fibermc.essentialcommands.mixin;

import com.fibermc.essentialcommands.playerdata.PlayerDataManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;

import static com.fibermc.essentialcommands.EssentialCommands.BACKING_CONFIG;

@Mixin(ClientboundPlayerInfoUpdatePacket.Action.class)
public class ClientboundPlayerInfoUpdatePacketActionMixin {

    @Mutable
    @Final
    @Shadow
    ClientboundPlayerInfoUpdatePacket.Action.Writer writer;

    @Inject(method = "<init>", at = @At(value = "RETURN"))
    public void ctor(String string, int i, ClientboundPlayerInfoUpdatePacket.Action.Reader reader, ClientboundPlayerInfoUpdatePacket.Action.Writer argWriter, CallbackInfo ci) {
        if (!"ADD_PLAYER".equals(string)) {
            return;
        }

        var vanillaWriter = writer;
        writer = (buf, entry) -> {
            // Need to use the backing config, since this lambda captures the field value.
            if (BACKING_CONFIG.NICKNAME_ABOVE_HEAD.getValue()) {
                var id = entry.profileId();
                var playerData = PlayerDataManager.getInstance().getByUuid(id);
                if (playerData == null) {
                    // playerData may return null in some cases.
                    // One known case is when a Taterzen from the Taterzens mod
                    // is added.
                    vanillaWriter.write(buf, entry);
                    return;
                }
                var displayName = playerData.getPlayer().getDisplayName();
                var displayNameString = displayName.getString(16);
                buf.writeUtf(displayNameString, 16);
                ByteBufCodecs.GAME_PROFILE_PROPERTIES.encode(buf, entry.profile().properties());
            } else {
                vanillaWriter.write(buf, entry);
            }
        };

    }
}
