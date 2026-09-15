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
import net.reggie.network.S2C.PistolS2CPayload;
import net.reggie.particle.ModParticles;

import java.util.List;

public class GomuPistolAbility implements IAbility {

    @Override public String getId() { return "gomu_pistol"; }

    @Override
    public String getName() {
        return Text.literal("Gomu Gomu no Pistol").formatted(Formatting.GOLD, Formatting.BOLD).getString();
    }

    @Override
    public String getDescription() {
        return "Streckt deinen Arm blitzschnell nach vorne, um Mobs aus der Distanz zu schlagen.";
    }

    @Override public boolean isPassive() { return false; }
    @Override public boolean isToggleable() { return false; }
    @Override public int getCooldownTicks() { return 80; }    // 4 Sekunden Abklingzeit
    @Override public float getCost() { return 15f; }          // Kostet 15 Energie

    @Override
    public void execute(ServerPlayerEntity player) {
        ServerWorld serverWorld = player.getServerWorld();
        var abilityComp = Redline.ABILITY_COMPONENT.get(player);

        // --- 1. KOSTEN- & COOLDOWN-PRÜFUNG ---
        if (!abilityComp.getCooldowns().isReady(getId())) return;

        // --- 2. SOUND-EFFEKT (Wuchtiger Peitschenschlag) ---
        serverWorld.playSound(
                null,
                player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_PLAYER_ATTACK_STRONG,
                SoundCategory.PLAYERS,
                1.5f, 0.6f
        );

        if (player instanceof ServerPlayerEntity serverPlayer) {
            ServerPlayNetworking.send(
                    serverPlayer,
                    new PistolS2CPayload()
            );
        }

        // --- 4. SERVER-HITBOX BERECHNUNG ---
        Vec3d eyePos = player.getEyePos();
        Vec3d lookVec = player.getRotationVec(1.0F);

        double attackRange = 12.0; // Reichweite der Pistol (12 Blöcke)
        DamageSource damageSource = serverWorld.getDamageSources().playerAttack(player);

        for (double d = 0.5; d <= attackRange; d += 0.5) {
            Vec3d hitCheckPos = eyePos.add(lookVec.multiply(d));

            // Erstellt die Hitboxen auf der virtuellen Flugbahn des Arms
            Box hitBox = new Box(
                    hitCheckPos.x - 0.5, hitCheckPos.y - 0.5, hitCheckPos.z - 0.5,
                    hitCheckPos.x + 0.5, hitCheckPos.y + 0.5, hitCheckPos.z + 0.5
            );

            List<LivingEntity> targets = serverWorld.getEntitiesByClass(LivingEntity.class, hitBox, entity -> entity != player);

            for (LivingEntity victim : targets) {
                // Doriki-Schadensbonus abrufen und anwenden
                double dorikiBonus = Redline.DORIKI.get(player).getDamageBonus();
                float finalDamage = 8.0f + (float) dorikiBonus;

                // Schaden zufügen
                victim.damage(damageSource, finalDamage);

                // Starker Rückstoß in Blickrichtung
                victim.takeKnockback(1.5, -lookVec.x, -lookVec.z);

                // Sound beim Treffer abspielen
                serverWorld.playSound(null, victim.getX(), victim.getY(), victim.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, SoundCategory.PLAYERS, 1.0f, 1.0f);
            }
        }
    }

    @Override
    public Identifier getIconTexture() {
        return Identifier.of(Redline.MOD_ID, "textures/abilities/gomu_pistol.png");
    }
}
