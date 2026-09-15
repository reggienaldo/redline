package net.reggie.game.abilities;

import java.util.HashMap;
import java.util.Map;

public class AbilityInventoryComposition {
    private final Map<String, String> gridInventory = new HashMap<>();

    // KORREKTUR: Die Slot-Größen (4 für links, 8 für rechts) müssen fest in den eckigen Klammern deklariert werden!
    private final String[] equippedSlots = new String[4]; // Links (Keybinds)
    private final String[] scrollSlots = new String[8];   // Rechts (Scroll-Leiste)

    public Map<String, String> getGridInventory() { return gridInventory; }
    public String[] getEquippedSlots() { return equippedSlots; }
    public String[] getScrollSlots() { return scrollSlots; }

    // Fügt die Fähigkeit an einer exakten X,Y-Koordinate im JJK-Raster hinzu
    public void unlockAbilityAt(int x, int y, String abilityId) {
        this.gridInventory.put(x + "," + y, abilityId);
    }

    public void unlockAbilityDynamically(String abilityId) {
        // Falls der Spieler die Fähigkeit schon gelernt hat, machen wir gar nichts
        if (this.hasUnlocked(abilityId)) return;

        // Wir scannen das Grid von Zeile 0 bis Zeile 100 ab (unendlich erweiterbar)
        for (int y = 0; y < 100; y++) {
            // Jede Zeile hat 4 Slots nebeneinander (Index 0, 1, 2, 3)
            for (int x = 0; x < 4; x++) {
                String key = x + "," + y;

                // Schaut nach, ob an dieser Koordinate noch absolut gar nichts registriert ist
                if (!this.gridInventory.containsKey(key)) {
                    // Perfekt, dieser Slot ist komplett ungenutzt!
                    this.gridInventory.put(key, abilityId);
                    return; // Beendet die gesamte Methode SOFORT, damit das nächste Item den nächsten Slot sucht!
                }
            }
        }
    }

    public boolean hasUnlocked(String abilityId) {
        return gridInventory.containsValue(abilityId);
    }

    public void equipToSlot(int slotIndex, String abilityId, boolean isKeybind) {
        String[] targetArray = isKeybind ? equippedSlots : scrollSlots;
        int maxSlots = isKeybind ? 4 : 8;

        if (slotIndex < 0 || slotIndex >= maxSlots) return;

        // Anti-Doppelbelegung (Löscht das Icon, falls es schon in dieser Leiste existiert)
        for (int i = 0; i < maxSlots; i++) {
            if (abilityId != null && abilityId.equals(targetArray[i])) {
                targetArray[i] = null;
            }
        }
        targetArray[slotIndex] = abilityId;
    }
}