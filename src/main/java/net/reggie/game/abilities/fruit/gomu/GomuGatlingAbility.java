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
import net.reggie.entity.ModEntities;
import net.reggie.entity.custom.GomuArmsEntity;
import net.reggie.game.abilities.IAbility;
import net.reggie.network.S2C.GatlingS2CPayload;
import net.reggie.network.S2C.GatlingStopS2CPayload;

import java.util.List;

public class GomuGatlingAbility implements IAbility {

    public static int activeGatlingTicks = 0;

    // UNFEHLBARER SPEICHER-TRICK: Merkt sich die gespawnte Entity direkt im RAM,
    // um fehleranfällige Mappings wie getPassengers vollständig zu umgehen!
    private static GomuArmsEntity currentGomuEntity = null;

    @Override public String getId() { return "gomu_gatling"; }

    @Override
    public String getName() {
        return Text.literal("Gomu Gomu no Gatling").formatted(Formatting.GOLD, Formatting.BOLD).getString();
    }

    @Override
    public String getDescription() {
        return "Entfesselt einen unaufhaltsamen Regen aus Schlägen. Drücke die Taste erneut, um vorzeitig abzubrechen.";
    }

    @Override public boolean isPassive() { return false; }
    @Override public boolean isToggleable() { return false; }
    @Override public int getCooldownTicks() { return 240; }   // 12 Sekunden Abklingzeit
    @Override public float getCost() { return 40f; }

    @Override
    public void execute(ServerPlayerEntity player) {
        var abilityComp = Redline.ABILITY_COMPONENT.get(player);
        ServerWorld serverWorld = player.getServerWorld();

        // =========================================================
        // 1. DER VORZEITIGE ABBRUCH-TRIGGER
        // =========================================================
        if (activeGatlingTicks > 0) {
            activeGatlingTicks = 0;

            // Despawnt die gespeicherte Entity sofort sicher
            if (currentGomuEntity != null && currentGomuEntity.isAlive()) {
                currentGomuEntity.discard();
                currentGomuEntity = null;
            }

            for (ServerPlayerEntity trackingPlayer : PlayerLookup.tracking(player)) {
                ServerPlayNetworking.send(trackingPlayer, new GatlingStopS2CPayload());
            }
            ServerPlayNetworking.send(player, new GatlingStopS2CPayload());

            player.sendMessage(Text.literal("§cGatling abgebrochen!"), true);
            return;
        }

        // --- NORMALER START (NUR WENN ABKLINGZEIT BEREIT IST) ---
        if (!abilityComp.getCooldowns().isReady(getId())) return;

        activeGatlingTicks = 60; // 3 Sekunden Dauer

        // =========================================================
        // 2. GOMU-ARMS-ENTITY AUF DEM SERVER SPAWNEN & AUFSITZEN
        // =========================================================
        if (!serverWorld.isClient()) {
            currentGomuEntity = new GomuArmsEntity(
                    ModEntities.GOMU_ARMS,
                    serverWorld
            );

            currentGomuEntity.refreshPositionAndAngles(
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    player.getYaw(),
                    player.getPitch()
            );

            currentGomuEntity.startRiding(player, true);
            serverWorld.spawnEntity(currentGomuEntity);
        }

        // =========================================================
        // 3. S2C ANIMATIONS SYNC (STARTET DIE CLIENT-POSE)
        // =========================================================
        for (ServerPlayerEntity trackingPlayer : PlayerLookup.tracking(player)) {
            ServerPlayNetworking.send(trackingPlayer, new GatlingS2CPayload());
        }
        ServerPlayNetworking.send(player, new GatlingS2CPayload());
    }

    public static void handleGatlingTick(ServerPlayerEntity player) {
        if (activeGatlingTicks <= 0) return;

        activeGatlingTicks--;
        ServerWorld serverWorld = player.getServerWorld();

        // =====================================================================
        // TRUE SCHULTER-ANDOCKUNG: TIEFER ZUM BODEN GEKOPPELT (ANIME ACCURATE)
        // =====================================================================
        if (!serverWorld.isClient() && currentGomuEntity != null && currentGomuEntity.isAlive()) {
            // Rechnet dein Yaw (Blickrichtung in Grad) in Radianten um
            float yawRad = (float) Math.toRadians(player.getYaw());

            // Berechnet die reine Vorwärts-Richtung auf der flachen X/Z Ebene
            double forwardX = -Math.sin(yawRad);
            double forwardZ = Math.cos(yawRad);

            // --- DIE PERFEKTE SCHULTER-KREUZUNG ---
            // 1. Wir gehen vom Spieler-Mittelpunkt aus.
            // 2. Wir schieben das Modell um 0.55 Blöcke nach vorne vor deine Brust.
            double targetX = player.getX() + (forwardX * 0.55);
            double targetZ = player.getZ() + (forwardZ * 0.55);

            // 3. CORE FIX: Geändert von -0.75 auf -1.25!
            // Das drückt die GomuArmsEntity extrem tief nach unten zum Erdboden hin,
            // damit sie perfekt mit deiner tiefen Blockbench-Hocke matcht!
            double targetY = player.getY() - 1.25;

            // Teleportiert die Entity auf den mathematisch perfekten Punkt flach über dem Boden
            currentGomuEntity.refreshPositionAndAngles(targetX, targetY, targetZ, player.getYaw(), player.getPitch());

            // Blickwinkel-Synchronisation für die Fäuste
            currentGomuEntity.setYaw(player.getYaw());
            currentGomuEntity.setPitch(player.getPitch());
            currentGomuEntity.setHeadYaw(player.getYaw());
            currentGomuEntity.setBodyYaw(player.getYaw());
            currentGomuEntity.velocityModified = true;
        }

        // =====================================================================
        // DAMAGE & SOUND LOGIK: Alle 2 Ticks Schaden und Maschinengewehr-Sounds
        // =====================================================================
        if (activeGatlingTicks % 2 == 0) {
            serverWorld.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, SoundCategory.PLAYERS, 1.0f, 1.5f);

            Vec3d lookVec = player.getRotationVec(1.0F);

            // Erstellt eine breite Suchbox vor dem Spieler
            Box searchBox = player.getBoundingBox().expand(7.0, 2.0, 7.0);

            // CORE-FIX: Ignoriert den Spieler UND die GomuArmsEntity vollständig bei der Hitbox-Abfrage!
            List<LivingEntity> targets = serverWorld.getEntitiesByClass(LivingEntity.class, searchBox, entity ->
                    entity != player && !(entity instanceof GomuArmsEntity)
            );

            DamageSource damageSource = serverWorld.getDamageSources().playerAttack(player);

            for (LivingEntity victim : targets) {
                Vec3d toVictim = victim.getPos().subtract(player.getPos()).normalize();
                double dotProduct = lookVec.dotProduct(toVictim);

                if (dotProduct > 0.5) {
                    double dorikiBonus = Redline.DORIKI.get(player).getDamageBonus();
                    float finalDamage = 2.0f + (float) (dorikiBonus * 0.25);

                    victim.damage(damageSource, finalDamage);
                    victim.takeKnockback(0.15, -lookVec.x, -lookVec.z);
                }
            }
        }

        // =====================================================================
        // AUTOMATISCHES ENDE: Wenn die 3 Sekunden natürlich abgelaufen sind
        // =====================================================================
        if (activeGatlingTicks == 0) {
            if (!serverWorld.isClient() && currentGomuEntity != null) {
                currentGomuEntity.discard();
                currentGomuEntity = null;
            }

            for (ServerPlayerEntity trackingPlayer : PlayerLookup.tracking(player)) {
                ServerPlayNetworking.send(trackingPlayer, new GatlingStopS2CPayload());
            }
            ServerPlayNetworking.send(player, new GatlingStopS2CPayload());
        }
    }

    public static boolean isChannelling() {
        return activeGatlingTicks > 0;
    }

    @Override
    public Identifier getIconTexture() {
        return Identifier.of(Redline.MOD_ID, "textures/abilities/gomu_gatling.png");
    }
}