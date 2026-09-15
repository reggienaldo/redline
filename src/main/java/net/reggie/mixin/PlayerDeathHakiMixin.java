package net.reggie.mixin;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.reggie.Redline;
import net.reggie.game.haki.IHakiComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class PlayerDeathHakiMixin {

    @Inject(method = "onDeath", at = @At("HEAD"))
    private void redline$deactivateHakiOnDeath(DamageSource source, CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;

        // Holt deine flache Haki-Komponente über den zentralen Redline-Key
        IHakiComponent hakiComp = Redline.HAKI.get(player);

        // Schaltet alle aktiven Haki-Formen beim Tod des Spielers augenblicklich aus
        hakiComp.setBusoActive(false);
        hakiComp.setKenActive(false);

        // Das manuelle Aufrufen von .sync() ist hier nicht mehr nötig,
        // da setBusoActive und setKenActive in deiner HakiComponentImpl bereits Redline.HAKI.sync(player) auslösen!
    }
}