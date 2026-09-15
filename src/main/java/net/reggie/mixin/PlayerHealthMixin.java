package net.reggie.mixin;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.reggie.Redline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class PlayerHealthMixin {

    @Inject(method = "getMaxHealth", at = @At("RETURN"), cancellable = true)
    private void injectDorikiMaxHealth(CallbackInfoReturnable<Float> cir) {
        if ((Object) this instanceof PlayerEntity player) {
            float baseMaxHealth = cir.getReturnValue();

            // Holt deine Doriki-Komponente
            var dorikiComp = Redline.DORIKI.get(player);

            // Rechnet den mathematisch gerundeten Doriki-Bonus auf die maximalen Herzen drauf
            float finalMaxHealth = baseMaxHealth + (float) dorikiComp.getHealthBonus();

            // Überschreibt den Rückgabetyp für Minecraft
            cir.setReturnValue(finalMaxHealth);
        }
    }
}
