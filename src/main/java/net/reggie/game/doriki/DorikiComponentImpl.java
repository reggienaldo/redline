package net.reggie.game.doriki;

import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.reggie.Redline;

public class DorikiComponentImpl implements IDorikiComponent {
    private final PlayerEntity player;
    private long doriki = 0;

    public DorikiComponentImpl(PlayerEntity player) {
        this.player = player;
    }

    @Override public long getDoriki() { return this.doriki; }

    @Override
    public void setDoriki(long value) {
        if (player.getWorld().isClient()) {
            this.doriki = Math.max(0, value); // --- LIMIT 10.000 ENTFERNT ---
            return;
        }

        float oldMaxHealth = player.getMaxHealth();
        boolean wasFullyHealed = player.getHealth() >= oldMaxHealth;

        this.doriki = Math.max(0, value); // --- LIMIT 10.000 ENTFERNT ---

        // CCA-Sync an den Client senden
        Redline.DORIKI.sync(this.player);

        // Heilt den Spieler bei Erhöhung der maximalen Herzen sofort mit auf
        float newMaxHealth = player.getMaxHealth();
        if (wasFullyHealed && newMaxHealth > oldMaxHealth) {
            player.setHealth(newMaxHealth);
        }
    }

    @Override public void addDoriki(long amount) { setDoriki(this.doriki + amount); }

    // --- ANIME-WURZEL-SKALIERUNG (UNBEGRENZT & REBOOT-SICHER) ---
    // Nutzt dieselbe mathematische Logik wie dein Haki-XP-System
    @Override
    public double getHealthBonus() {
        // Beispiel: 1.000 Doriki = ~6.3 HP (+3 Herzen)
        //          10.000 Doriki = 20.0 HP (+10 Herzen)
        //          40.000 Doriki = 40.0 HP (+20 Herzen) -> Skaliert unendlich weiter!
        // Math.floor(X / 2) * 2 erzwingt, dass der Bonus IMMER eine gerade Zahl ist (Ganze Herzen)
        double rawBonus = Math.sqrt(this.doriki) * 0.2;
        return Math.floor(rawBonus / 2.0) * 2.0;
    }

    @Override
    public double getDamageBonus() {
        // Beispiel: 1.000 Doriki = +3.1 Schaden
        //          10.000 Doriki = +10.0 Schaden
        //          40.000 Doriki = +20.0 Schaden
        return Math.floor(Math.sqrt(this.doriki) * 0.1);
    }

    @Override
    public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registries) {
        this.doriki = tag.getLong("DorikiPoints");
    }

    @Override
    public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registries) {
        tag.putLong("DorikiPoints", this.doriki);
    }
}