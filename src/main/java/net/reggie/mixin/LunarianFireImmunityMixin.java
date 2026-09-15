package net.reggie.mixin;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.reggie.Redline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class LunarianFireImmunityMixin {

    @Inject(method = "isInvulnerableTo", at = @At("HEAD"), cancellable = true)
    private void injectLunarianFireImmunity(DamageSource damageSource, CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        var raceComp = Redline.RACE.get(player);

        // Wenn der Spieler ein Lunarian ist und Feuerschaden (Lava, Magma, Brennen) erleidet -> IMMUN
        if (raceComp.isLunarian() && (damageSource.isIn(net.minecraft.registry.tag.DamageTypeTags.IS_FIRE))) {
            cir.setReturnValue(true);
        }
    }
}