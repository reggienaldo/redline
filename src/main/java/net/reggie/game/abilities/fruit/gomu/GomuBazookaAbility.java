package net.reggie.game.abilities.fruit.gomu;

import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.reggie.Redline;
import net.reggie.game.abilities.IAbility;
import net.reggie.network.S2C.BazookaS2CPayload;

import java.util.List;

public class GomuBazookaAbility implements IAbility {

    @Override public String getId() { return "gomu_bazooka"; }

    @Override
    public String getName() {
        return Text.literal("Gomu Gomu no Bazooka").formatted(Formatting.GOLD, Formatting.BOLD).getString();
    }

    @Override
    public String getDescription() {
        return "Zieht beide Arme weit nach hinten, um Gegner mit einer verheerenden Schockwelle wegzupfeffern.";
    }

    @Override public boolean isPassive() { return false; }
    @Override public boolean isToggleable() { return false; }
    @Override public int getCooldownTicks() { return 160; }   // 8 Sekunden Abklingzeit
    @Override public float getCost() { return 30f; }          // Kostet 30 Energie im Kampf

    @Override
    public void execute(ServerPlayerEntity player) {
        ServerWorld serverWorld = player.getServerWorld();
        var abilityComp = Redline.ABILITY_COMPONENT.get(player);

        // --- 1. COOLDOWN & KOSTEN CONTROLLE ---
        if (!abilityComp.getCooldowns().isReady(getId())) return;

        // --- 2. S2C ANIMATIONS SYNC (UNIT-PAYLOAD STABIL) ---
        // Schickt das leere Paket an alle Tracker und dich selbst für maximale Synchronisation
        for (ServerPlayerEntity trackingPlayer : PlayerLookup.tracking(player)) {
            ServerPlayNetworking.send(trackingPlayer, new BazookaS2CPayload());
        }
        ServerPlayNetworking.send(player, new BazookaS2CPayload());

        // --- 3. EXECUTION SOUNDS (Explosions-Wucht) ---
        serverWorld.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.PLAYERS, 1.2f, 1.4f);
        serverWorld.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, SoundCategory.PLAYERS, 1.5f, 0.5f);

        // --- 4. BREITE FLÄCHEN-HITBOX VOR DEM SPIELER (CONE BERECHNUNG) ---
        Vec3d eyePos = player.getEyePos();
        Vec3d lookVec = player.getRotationVec(1.0F);

        // Erstellt eine große Such-Box um den Spieler herum (6 Blöcke Reichweite)
        Box searchBox = player.getBoundingBox().expand(6.0, 3.0, 6.0);
        List<LivingEntity> targets = serverWorld.getEntitiesByClass(LivingEntity.class, searchBox, entity -> entity != player);

        DamageSource damageSource = serverWorld.getDamageSources().playerAttack(player);

        for (LivingEntity victim : targets) {
            Vec3d toVictim = victim.getPos().subtract(player.getPos()).normalize();

            // Mathematischer Winkel-Check: Trifft den Gegner nur, wenn er sich vor dem Spieler befindet (ca. 70 Grad Blickfeld)
            double dotProduct = lookVec.dotProduct(toVictim);
            if (dotProduct > 0.45) {

                // Doriki- und Kampfstil-Schadensberechnung einfließen lassen!
                double dorikiBonus = Redline.DORIKI.get(player).getDamageBonus();
                float finalDamage = 14.0f + (float) dorikiBonus; // 14 Basis-Schaden (7 ganze Herzen!) + RPG-Scaling

                // Schaden zufügen
                victim.damage(damageSource, finalDamage);

                // --- BRUTALER ANIME KNOCKBACK ---
                // Schießt den Gegner extrem weit nach hinten und steil nach oben in die Wolken!
                victim.takeKnockback(3.5, -lookVec.x, -lookVec.z);
                victim.setVelocity(victim.getVelocity().x * 2.5, 0.75, victim.getVelocity().z * 2.5);
                victim.velocityModified = true; // Zwingt den Client des Mobs zur Positionsänderung

                // Einschlag-Sound am Mob abspielen
                serverWorld.playSound(null, victim.getX(), victim.getY(), victim.getZ(), SoundEvents.ENTITY_DRAGON_FIREBALL_EXPLODE, SoundCategory.PLAYERS, 1.0f, 0.8f);
            }
        }
    }

    @Override
    public Identifier getIconTexture() {
        return Identifier.of(Redline.MOD_ID, "textures/abilities/gomu_bazooka.png");
    }
}
