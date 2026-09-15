package net.reggie.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.reggie.game.haki.HakiCombatHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void injectHakiDodge(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        Object obj = this;
        if (obj instanceof PlayerEntity player) {
            // Wenn onDamageReceived true zurückgibt, weicht der Spieler aus
            if (HakiCombatHandler.onDamageReceived(player, source, amount)) {
                cir.setReturnValue(false);
                cir.cancel();
            }
        }
    }
}
