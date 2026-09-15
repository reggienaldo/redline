package net.reggie.entity.custom;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.instance.SingletonAnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

public class GomuArmsEntity extends PathAwareEntity implements GeoEntity {
    private final AnimatableInstanceCache cache = new SingletonAnimatableInstanceCache(this);

    public GomuArmsEntity(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
    }

    // =========================================================
    // ERFORDRLICHE ATTRIBUTE FÜR 1.21.1 (FIX GEGEN SPAWN-CRASH)
    // =========================================================
    public static DefaultAttributeContainer.Builder setAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 20.0)       // Standard-Spielerleben (10 Herzen)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.25)    // Normale Laufgeschwindigkeit
                .add(EntityAttributes.GENERIC_ARMOR, 0.0)             // Basis-Rüstung
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 16.0);    // Reichweite für Wegfindung
    }

    @Override
    protected void updatePassengerPosition(net.minecraft.entity.Entity passenger, PositionUpdater positionUpdater) {
        // Da die Position bereits perfekt im handleGatlingTick nach vorne verschoben wird,
        // blockieren wir hier Minecrafts automatische Rückpositionierung komplett!
        if (passenger == this.getVehicle()) {
            // Lässt den Server die im Tick berechneten Koordinaten beibehalten
            return;
        }
        super.updatePassengerPosition(passenger, positionUpdater);
    }

    @Override
    public void tick() {
        super.tick();
        // Optionale Server-Logik: Die Entity stirbt automatisch nach 3 Sekunden (60 Ticks),
        // wenn die Gatling vorbei ist!
        if (!this.getWorld().isClient() && this.age > 60) {
            this.discard();
        }
    }

    // --- GECKOLIB 4.5 ANIMATIONS-STEUERUNG ---
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    private <T extends GeoAnimatable> PlayState predicate(AnimationState<T> state) {
        state.getController().setAnimation(
                RawAnimation.begin().then("idle", Animation.LoopType.LOOP)
        );
        return PlayState.CONTINUE;
    }

    // Verhindert, dass die Entity eine physische Hitbox in der Welt hat (Mobs laufen durch sie durch)
    @Override
    public boolean isCollidable() {
        return false;
    }

    // Macht die Entity komplett immun gegen jegliche Art von Schaden (Schläge, Projektile, Explosionen)
    @Override
    public boolean isInvulnerableTo(net.minecraft.entity.damage.DamageSource damageSource) {
        return true;
    }

    // Verhindert, dass der Spieler oder andere Mobs die Entity im Kampf anvisieren oder schlagen können
    @Override
    public boolean isAttackable() {
        return false;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
