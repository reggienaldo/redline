package net.reggie.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.reggie.game.haki.HakiCombatHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
    @ModifyVariable(method = "attack", at = @At(value = "STORE", ordinal = 0), name = "f")
    private float injectHakiDamage(float baseDamage, Entity target) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        return HakiCombatHandler.onAttack(player, target, baseDamage, player.getDamageSources().playerAttack(player));
    }
}
