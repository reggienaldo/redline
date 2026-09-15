package net.reggie.game.haki;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.reggie.Redline;
import net.reggie.particle.ModParticles;
import net.reggie.sound.ModSounds;

public class HakiCombatHandler {

    public static float onAttack(PlayerEntity attacker, Entity target, float baseDamage, DamageSource source) {
        IHakiComponent haki = Redline.HAKI.get(attacker);
        float finalDamage = baseDamage;

        if (haki.isBusoActive()) {
            var world = attacker.getWorld();

            // 1. Stufe: Hardening (Schadens-Boost läuft synchron auf Client und Server)
            finalDamage += 2.0f + (haki.getBusoLevel() * 0.5f);

            // Logik-Zuweisungen, Sounds und Partikel NUR auf dem Server ausführen!
            if (!world.isClient() && world instanceof ServerWorld serverWorld) {
                haki.addBusoXp(8);

                // Positionsdaten des getroffenen Gegners für die Partikel
                double tx = target.getX();
                double ty = target.getBodyY(0.5); // Mitte des Körpers
                double tz = target.getZ();

                // PRÜFUNG: Besitzt der Spieler das Königshaki-Coating?
                boolean isCoatingActive = haki.hasConquerorCoating();

                // 2. Stufe: Ryou (Partikel-Schockwelle) - Nur wenn KEIN Hao Coating aktiv ist
                if (haki.isRyouUnlocked()) {
                    haki.addBusoXp(12);

                    if (!isCoatingActive) {
                        serverWorld.spawnParticles(
                                ModParticles.RYOU_HIT,
                                tx, ty, tz,
                                1, 0.2, 0.2, 0.2, 0.15
                        );
                    }
                }

                // 3. Stufe: Emission - Sound und Poof-Partikel nur ohne Hao Coating
                if (haki.isEmissionUnlocked()) {
                    haki.addBusoXp(15);

                    if (!isCoatingActive) {
                        serverWorld.spawnParticles(ParticleTypes.POOF, tx, ty, tz, 5, 0.1, 0.1, 0.1, 0.05);
                        serverWorld.playSound(null, tx, ty, tz, ModSounds.GUARD_HAKI, SoundCategory.PLAYERS, 0.5f, 1.5f);
                    }
                }

                // 4. Stufe: Internal Destruction - Sound und Enchanter-Partikel nur ohne Hao Coating
                if (haki.isInternalDestructionUnlocked() && target instanceof LivingEntity) {
                    haki.addBusoXp(20);

                    if (!isCoatingActive) {
                        serverWorld.spawnParticles(ParticleTypes.ENCHANTED_HIT, tx, ty, tz, 20, 0.3, 0.3, 0.3, 0.2);
                        serverWorld.playSound(null, tx, ty, tz, ModSounds.GUARD_HAKI, SoundCategory.PLAYERS, 0.5f, 1.5f);
                    }
                }

                // 5. Stufe: Haoshoku Coating (Mächtige Königshaki-Blitze überschreiben alles andere!)
                if (isCoatingActive) {
                    haki.addHaoXp(25);

                    // --- HAOSHOKU COATING BLITZE ---
                    serverWorld.spawnParticles(
                            ModParticles.CONQ_HIT,
                            tx, ty, tz,
                            1, 0.4, 0.5, 0.4, 0.05
                    );

                    // Exklusiver dumpfer Explosionssound für den Coating-Treffer
                    serverWorld.playSound(null, tx, ty, tz, ModSounds.HAO_HAKI, SoundCategory.PLAYERS, 0.5f, 1.5f);
                }
            }

            // Schadens-Multiplikatoren für die Stufen berechnen (Bleiben für die Schadensberechnung alle aktiv)
            if (haki.isRyouUnlocked()) finalDamage *= 1.15f;
            if (haki.isEmissionUnlocked()) finalDamage += 3.0f;
            if (haki.isInternalDestructionUnlocked() && target instanceof LivingEntity livingTarget) {
                finalDamage += (livingTarget.getArmor() * 0.75f);
            }
            if (haki.hasConquerorCoating()) finalDamage *= 1.5f;
        }
        return finalDamage;
    }

    public static boolean onDamageReceived(PlayerEntity player, DamageSource source, float amount) {
        IHakiComponent haki = Redline.HAKI.get(player);
        var world = player.getWorld();

        if (haki.isKenActive()) {
            double dodgeChance = Math.min(0.35, 0.05 + (haki.getKenLevel() * 0.01));

            if (haki.isFutureSightUnlocked()) {
                dodgeChance = 0.65;
            } else if (haki.isAdvancedObservationUnlocked()) {
                dodgeChance += 0.15;
            }

            // Die Berechnung und XP-Vergabe läuft ausschließlich serverseitig!
            if (player.getRandom().nextDouble() < dodgeChance) {
                if (!world.isClient()) {
                    if (haki.isFutureSightUnlocked()) {
                        haki.consumeHaki(15f);
                        haki.addKenXp(30); // Belohnung für perfekten Zukunftssicht-Dodge
                    } else if (haki.isAdvancedObservationUnlocked()) {
                        haki.addKenXp(15);
                    } else {
                        haki.addKenXp(8);
                    }
                }
                return true; // Ausgewichen! Schaden abgebrochen
            }
        }

        // Auch das Einstecken von Treffern mit aktivem Rüstungshaki härtet ab und gibt XP
        if (!world.isClient() && haki.isBusoActive()) {
            haki.addBusoXp(4);
        }
        return false;
    }
}