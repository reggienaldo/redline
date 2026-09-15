package net.reggie.mixin;

import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.reggie.Redline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class WaterWeaknessMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void sinkInWater(CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;

        if (entity instanceof PlayerEntity player) {
            if (player.isCreative()) return;

            var fruit = Redline.DEVIL_FRUIT.get(player);
            if (!fruit.hasFruit()) return;

            if (player.isTouchingWater()) {

                // --- SERVER-LOGIK: DEAKTIVIERT NUR DIE AUREN, SPERRT DIE ENERGIE ABER NICHT ---
                if (!player.getWorld().isClient()) {
                    var hakiComp = Redline.HAKI.get(player);

                    // Wenn eine Aura aktiv ist, schalten wir sie einfach aus
                    if (hakiComp.isBusoActive() || hakiComp.isKenActive() || hakiComp.isHaoActive()) {
                        hakiComp.setBusoActive(false);
                        hakiComp.setKenActive(false);
                        hakiComp.setHaoActive(false);

                        // Sofortiger Sync, damit der Client die Deaktivierung sieht
                        Redline.HAKI.sync(player);
                        player.sendMessage(net.minecraft.text.Text.literal("§cDas Wasser lässt dein Haki erlöschen!"), true);
                    }
                }

                // --- DEINE PHYSK-SINK-LOGIK ---
                player.setPose(EntityPose.SWIMMING);
                Vec3d vel = player.getVelocity();

                player.setVelocity(
                        vel.x * 0.2,
                        -0.3,
                        vel.z * 0.2
                );

                player.fallDistance = 0;
                player.setSwimming(false);
            }
        }
    }
}