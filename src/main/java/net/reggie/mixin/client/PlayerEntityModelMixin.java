package net.reggie.mixin.client;

import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerModelPart;
import net.reggie.Redline;
import net.reggie.game.haki.IHakiComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityModel.class)
public abstract class PlayerEntityModelMixin<T extends LivingEntity> {

    @Inject(method = "setAngles(Lnet/minecraft/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
    private void redline$hideSleeves(T entity, float limbAngle, float limbDistance, float animationProgress, float headYaw, float headPitch, CallbackInfo ci) {
        if (!(entity instanceof PlayerEntity player)) return;

        // Holt deine flache Haki-Komponente über den zentralen Redline-Key
        IHakiComponent hakiComp = Redline.HAKI.get(player);
        PlayerEntityModel<?> model = (PlayerEntityModel<?>)(Object)this;

        // Prüfen, ob das Rüstungshaki (Busoshoku) aktiv geschaltet ist
        if (hakiComp.isBusoActive()) {
            // Blendet die äußere Skin-Ebene (Ärmel) aus, damit die schwarze Haki-Textur/Shader voll sichtbar wird
            model.leftSleeve.visible = false;
            model.rightSleeve.visible = false;
        } else {
            // Setzt die Sichtbarkeit auf den Standard-Zustand zurück, den der Spieler gewählt hat
            model.leftSleeve.visible = player.isPartVisible(PlayerModelPart.LEFT_SLEEVE);
            model.rightSleeve.visible = player.isPartVisible(PlayerModelPart.RIGHT_SLEEVE);
        }
    }
}