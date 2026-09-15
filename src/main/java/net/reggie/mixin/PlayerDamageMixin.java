package net.reggie.mixin;

import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.reggie.Redline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public class PlayerDamageMixin {

    // FEHLERFREIER 1.21.1 FIX: Wir hängen uns direkt in die Angriffs-Methode "attack" ein.
    // Minecraft berechnet dort eine lokale Float-Variable "f" (das ist der finale Schaden).
    // Wir modifizieren diese Variable und rechnen deinen Doriki-Bonus flach obendrauf!
    @ModifyVariable(
            method = "attack",
            at = @At(value = "STORE", ordinal = 0),
            ordinal = 0
    )
    private float modifyDorikiAttackDamage(float baseDamage) {
        PlayerEntity player = (PlayerEntity) (Object) this;

        // Holt deine Doriki-Komponente
        var dorikiComp = Redline.DORIKI.get(player);

        // Addiert deinen Doriki-Stärkebonus direkt auf den berechneten Schaden
        return baseDamage + (float) dorikiComp.getDamageBonus();
    }
}
