package net.reggie.game.haki;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.reggie.Redline;
import net.reggie.particle.ModParticles;
import net.reggie.sound.ModSounds;

public class HakiComponentImpl implements IHakiComponent {
    private final PlayerEntity player;

    private boolean haoActive = false;
    private float hakiEnergy = 100f;

    private int auraCdDisplayTicks = 0;

    // Freischaltungs-Zustände (Müssen gelernt werden)
    private boolean busoUnlocked = false;
    private boolean kenUnlocked = false;

    private int ryouColorIndex = 0; // Standardmäßig 0 (Rot)

    private boolean busoActive = false;
    private long busoXp = 0;

    private boolean kenActive = false;
    private long kenXp = 0;

    private boolean haoUnlocked = false;
    private long haoXp = 0;

    public static final long XP_RYOU = 1000;
    public static final long XP_EMISSION = 5000;
    public static final long XP_INTERNAL_DESTRUCTION = 12000;
    public static final long XP_ADVANCED_OBSERVATION = 2500;
    public static final long XP_FUTURE_SIGHT = 7500;
    public static final long XP_CONQUEROR_AURA = 4000;
    public static final long XP_CONQUEROR_COATING = 15000;

    public HakiComponentImpl(PlayerEntity player) { this.player = player; }

    @Override public long getTotalXp() { return this.busoXp + this.kenXp + this.haoXp; }
    @Override public float getMaxHaki() { return 100f + (getTotalXp() / 50f); }
    @Override public float getHakiRegenRate() { return 0.05f + (getMaxHaki() * 0.0005f); }
    @Override public float getHaki() { return this.hakiEnergy; }

    @Override
    public void setHaki(float value) {
        this.hakiEnergy = Math.max(0f, Math.min(getMaxHaki(), value));
        Redline.HAKI.sync(this.player);
    }

    @Override public void addHaki(float amount) { setHaki(this.hakiEnergy + amount); }
    @Override public void consumeHaki(float amount) { setHaki(this.hakiEnergy - amount); }
    @Override public boolean canUseHaki(float amount) { return this.hakiEnergy >= amount; }

    @Override public boolean isHaoActive() { return this.haoActive; }

    // --- GEFIXT: Logik sauber getrennt, um den && Operator-Fehler zu beheben ---
    @Override
    public void setHaoActive(boolean active) {
        this.haoActive = this.haoUnlocked && active;
        Redline.HAKI.sync(player);
    }

    @Override public int getRyouColorIndex() { return this.ryouColorIndex; }

    @Override
    public void setRyouColorIndex(int index) {
        this.ryouColorIndex = index;
        Redline.HAKI.sync(this.player);
    }

    // Hilfsmethode für den sekündlichen Schockwellen-Stoß deines Königshakis
    private void triggerAuraPulse() {
        if (!(player.getWorld() instanceof ServerWorld serverWorld)) return;

        int haoLevel = getHaoLevel();
        double radius = 6.0 + (haoLevel * 0.4);

        // --- 1. DER WUCHTIGE ANIME SOUND ---
        serverWorld.playSound(
                null,
                player.getX(), player.getY(), player.getZ(),
                ModSounds.HAO_HAKI,
                SoundCategory.PLAYERS,
                1.1f, 0.85f
        );

        // --- 2. GENERIERUNG DER RIESIGEN EINZEL-BLITZE ---
        int lightningCount = 4 + player.getRandom().nextInt(3); // 4 bis 7 Einzelblitze pro Puls

        for (net.minecraft.server.network.ServerPlayerEntity serverPlayer : serverWorld.getPlayers()) {

            for (int k = 0; k < lightningCount; k++) {
                // Bestimmt einen zufälligen Winkel im Radius um den Spieler
                double angle = player.getRandom().nextDouble() * Math.PI * 2;
                double offsetX = Math.cos(angle) * (radius * 0.5);
                double offsetZ = Math.sin(angle) * (radius * 0.5);

                // Würfelt eine völlig freie Höhe in der Luft (zwischen Knien und 2.5 Blöcken Höhe)
                double startY = player.getY() + 0.2 + (player.getRandom().nextDouble() * 2.3);

                // SPAWNT EXAKT NUR EIN EINZIGES PARTIKEL!
                serverWorld.spawnParticles(
                        serverPlayer,
                        ModParticles.CONQ_LIGHTNING,
                        true, // FORCE = TRUE (wichtig)
                        player.getX() + offsetX,
                        startY,
                        player.getZ() + offsetZ,
                        1,                    // KORREKTUR: Nur noch genau 1 Partikel!
                        0.0, 0.0, 0.0,        // Keine Streuung mehr nötig, da die Fabrik rotiert
                        0.0                   // Geschwindigkeit 0, damit der Blitz starr steht
                );
            }
        }

        // --- 4. GEGNER IM UMKREIS ERFASSEN (Knockback & Schaden) ---
        java.util.List<LivingEntity> allTargets = serverWorld.getEntitiesByClass(
                LivingEntity.class,
                player.getBoundingBox().expand(radius),
                livingEntity -> true
        );

        DamageSource damageSource = serverWorld.getDamageSources().playerAttack(player);

        for (LivingEntity victim : allTargets) {
            if (victim == player) continue;

            double deltaX = victim.getX() - player.getX();
            double deltaZ = victim.getZ() - player.getZ();
            double distance = Math.max(0.1, Math.sqrt(deltaX * deltaX + deltaZ * deltaZ));

            victim.takeKnockback(1.2, -deltaX / distance, -deltaZ / distance);
            victim.damage(damageSource, 2.0f + (haoLevel * 0.5f));

            this.addHaoXp(5);
        }
    }

    @Override public int getAuraCdDisplayTicks() { return this.auraCdDisplayTicks; }
    @Override public void showAuraCdDisplay(int ticks) { this.auraCdDisplayTicks = ticks; }

    @Override
    public void tick() {
        // --- CRITICAL HUD FIX: Lässt die Cooldowns live auf dem Client-Bildschirm herunterzählen ---
        // Dieser Block MUSS zwingend vor der Client-Schranke stehen!
        var localAbilityComp = Redline.ABILITY_COMPONENT.get(player);
        if (localAbilityComp != null && localAbilityComp.getCooldowns() != null) {
            localAbilityComp.getCooldowns().tick(); // Ruft deine Tick-Logik für Integers auf dem Client auf
        }

        // Client-Schranke für die serverseitige Logik
        if (player.getWorld().isClient()) return;

        // --- AUTOMATISCHE AURA-FREISCHALTUNG FÜR DAS RASTER-INVENTAR ---
        var inventory = localAbilityComp.getInventory();

        if (this.isHaoUnlocked()) {
            if (!inventory.hasUnlocked("conq_aura")) {
                inventory.unlockAbilityDynamically("conq_aura");
                Redline.ABILITY_COMPONENT.sync(player);
            }
        } else {
            if (inventory.hasUnlocked("conq_aura")) {
                inventory.getGridInventory().values().removeIf(id -> id.equals("conq_aura"));
                Redline.ABILITY_COMPONENT.sync(player);
            }
        }

        // --- COMBAT MODE CHECK ---
        var combatComp = Redline.COMBAT_COMPONENT.get(player);

        if (!combatComp.isCombatModeEnabled()) {
            if (busoActive || kenActive || haoActive) {
                // Cooldown anwenden, falls die Aura durch Deaktivieren des Kampfmodus abbricht
                if (haoActive) {
                    localAbilityComp.getCooldowns().setCooldown("conq_aura", 200);
                    Redline.ABILITY_COMPONENT.sync(player);
                }

                this.busoActive = false;
                this.kenActive = false;
                this.haoActive = false;
                Redline.HAKI.sync(player);
            }
            return;
        }

        // --- HAKI ENERGIE-VERBRAUCH PRO TICK ---
        boolean operational = false;

        if (busoActive) {
            float cost = 0.1f + (getBusoLevel() * 0.02f);

            if (canUseHaki(cost)) {
                consumeHaki(cost);
                operational = true;
            } else {
                setBusoActive(false);
            }
        }

        if (kenActive) {
            float cost = 0.08f + (getKenLevel() * 0.015f);

            if (canUseHaki(cost)) {
                consumeHaki(cost);
                operational = true;
            } else {
                setKenActive(false);
            }
        }

        if (haoActive) {
            float cost = 0.15f + (getHaoLevel() * 0.03f);

            if (canUseHaki(cost)) {
                consumeHaki(cost);
                operational = true;

                // Alle 20 Ticks (1 Sekunde) wird die Druckwelle um den Spieler freigesetzt
                if (player.age % 20 == 0) {
                    this.triggerAuraPulse();
                }
            } else {
                // --- ERZWUNGENER ABBRUCH BEI ENERGIEMANGEL ---
                setHaoActive(false);

                // Setzt den Cooldown sicher auf 200 Ticks (10 Sekunden) in deiner Tick-Composition
                localAbilityComp.getCooldowns().setCooldown("conq_aura", 200);

                // Synchronisiert die Änderung sofort mit dem Client, damit das HUD anfängt zu zählen
                Redline.ABILITY_COMPONENT.sync(player);

                player.sendMessage(Text.literal("§cAura abgebrochen! Keine Haki-Energie mehr."), true);
            }
        }

        // Automatische Regeneration, wenn kein Haki aktiv verwendet wird
        if (!operational && this.hakiEnergy < getMaxHaki()) {
            addHaki(getHakiRegenRate());
        }
    }

    // --- BUSOSHOKU ---
    @Override public boolean isBusoUnlocked() { return busoUnlocked; }

    @Override
    public void setBusoUnlocked(boolean unlocked) {
        this.busoUnlocked = unlocked;
        Redline.HAKI.sync(player);
    }

    @Override public boolean isBusoActive() { return busoActive; }

    @Override
    public void setBusoActive(boolean active) {
        this.busoActive = busoUnlocked && active;
        Redline.HAKI.sync(player);
    }

    @Override public long getBusoXp() { return busoXp; }

    @Override
    public void addBusoXp(long amount) {
        if(busoUnlocked) {
            this.busoXp = Math.max(0, this.busoXp + amount);
            Redline.HAKI.sync(player);
        }
    }

    @Override public int getBusoLevel() { return (int) (Math.sqrt(busoXp) / 10) + 1; }

    @Override public boolean isRyouUnlocked() { return busoXp >= XP_RYOU; }

    @Override public boolean isEmissionUnlocked() { return busoXp >= XP_EMISSION; }

    @Override
    public boolean isInternalDestructionUnlocked() {
        return busoXp >= XP_INTERNAL_DESTRUCTION;
    }

    // --- KENBUNSHOKU ---
    @Override public boolean isKenUnlocked() { return kenUnlocked; }

    @Override
    public void setKenUnlocked(boolean unlocked) {
        this.kenUnlocked = unlocked;
        Redline.HAKI.sync(player);
    }

    @Override public boolean isKenActive() { return kenActive; }

    @Override
    public void setKenActive(boolean active) {
        this.kenActive = kenUnlocked && active;
        Redline.HAKI.sync(player);
    }

    @Override public long getKenXp() { return kenXp; }

    @Override
    public void addKenXp(long amount) {
        if(kenUnlocked) {
            this.kenXp = Math.max(0, this.kenXp + amount);
            Redline.HAKI.sync(player);
        }
    }

    @Override public int getKenLevel() { return (int) (Math.sqrt(kenXp) / 10) + 1; }

    @Override
    public boolean isAdvancedObservationUnlocked() {
        return kenXp >= XP_ADVANCED_OBSERVATION;
    }

    @Override
    public boolean isFutureSightUnlocked() {
        return kenXp >= XP_FUTURE_SIGHT;
    }

    // --- HAOSHOKU ---
    @Override public boolean isHaoUnlocked() { return haoUnlocked; }

    @Override
    public void setHaoUnlocked(boolean unlocked) {
        this.haoUnlocked = unlocked;
        Redline.HAKI.sync(player);
    }

    @Override public long getHaoXp() { return haoXp; }

    @Override
    public void addHaoXp(long amount) {
        if(haoUnlocked) {
            this.haoXp = Math.max(0, this.haoXp + amount);
            Redline.HAKI.sync(player);
        }
    }

    @Override public int getHaoLevel() { return (int) (Math.sqrt(haoXp) / 15) + 1; }

    @Override
    public boolean hasConquerorAura() {
        return haoUnlocked && haoXp >= XP_CONQUEROR_AURA;
    }

    @Override
    public boolean hasConquerorCoating() {
        return haoUnlocked && haoXp >= XP_CONQUEROR_COATING;
    }

    // --- NBT ---
    @Override
    public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registries) {
        this.hakiEnergy = tag.getFloat("HakiEnergy");
        this.busoUnlocked = tag.getBoolean("BusoUnlocked");
        this.kenUnlocked = tag.getBoolean("KenUnlocked");
        this.busoActive = tag.getBoolean("BusoActive");
        this.busoXp = tag.getLong("BusoXp");

        this.kenActive = tag.getBoolean("KenActive");
        this.kenXp = tag.getLong("KenXp");

        this.haoUnlocked = tag.getBoolean("HaoUnlocked");
        this.haoXp = tag.getLong("HaoXp");
        this.haoActive = tag.getBoolean("HaoActive");

        if (tag.contains("RyouColorIndex")) {
            this.ryouColorIndex = tag.getInt("RyouColorIndex");
        } else {
            this.ryouColorIndex = player.getRandom().nextInt(7);
        }
    }

    @Override
    public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registries) {
        tag.putFloat("HakiEnergy", this.hakiEnergy);
        tag.putBoolean("BusoUnlocked", this.busoUnlocked);
        tag.putBoolean("KenUnlocked", this.kenUnlocked);
        tag.putBoolean("BusoActive", this.busoActive);
        tag.putLong("BusoXp", this.busoXp);

        tag.putBoolean("KenActive", this.kenActive);
        tag.putLong("KenXp", this.kenXp);

        tag.putBoolean("HaoUnlocked", this.haoUnlocked);
        tag.putLong("HaoXp", this.haoXp);
        tag.putBoolean("HaoActive", this.haoActive);

        tag.putInt("RyouColorIndex", this.ryouColorIndex);
    }
}