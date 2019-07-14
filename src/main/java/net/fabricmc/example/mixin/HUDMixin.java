package net.fabricmc.example.mixin;

@Mixin(InGameHud.class)
public abstract class HUDMixin {
    
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;dropItems()V"), method = "interactMob", cancellable = true)
    private void onShear(final PlayerEntity player, final Hand hand, final CallbackInfoReturnable<Boolean> info) {
        ActionResult result = SheepShearCallback.EVENT.invoker().interact(player, (SheepEntity) (Object) this);
        if(result == ActionResult.FAIL) {
            info.cancel();
        }
    }
}