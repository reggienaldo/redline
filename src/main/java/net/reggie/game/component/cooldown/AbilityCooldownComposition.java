package net.reggie.game.component.cooldown;

import java.util.HashMap;
import java.util.Map;

public class AbilityCooldownComposition {
    // Speichert: Fähigkeiten-ID -> Verbleibende Ticks
    private final Map<String, Integer> cooldowns = new HashMap<>();

    public void setCooldown(String abilityId, int ticks) {
        if (ticks > 0) {
            cooldowns.put(abilityId, ticks);
        }
    }

    public int getRemainingTicks(String abilityId) {
        return cooldowns.getOrDefault(abilityId, 0);
    }

    public boolean isReady(String abilityId) {
        return getRemainingTicks(abilityId) <= 0;
    }

    // Wird vom Server-Tick aufgerufen, um die Zeit runterzuzählen
    public void tick() {
        cooldowns.entrySet().removeIf(entry -> {
            int nextTicks = entry.getValue() - 1;
            if (nextTicks <= 0) {
                return true; // Cooldown vorbei, Eintrag löschen
            }
            entry.setValue(nextTicks);
            return false;
        });
    }

    // Für das NBT-System (beim Einloggen/Ausloggen)
    public Map<String, Integer> getCooldownMap() { return cooldowns; }
}