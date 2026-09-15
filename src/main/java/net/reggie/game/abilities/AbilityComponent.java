package net.reggie.game.abilities;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.reggie.Redline;
import net.reggie.game.abilities.fruit.gomu.GomuGatlingAbility;
import net.reggie.game.component.cooldown.AbilityCooldownComposition;
import org.ladysnake.cca.api.v3.component.ComponentV3;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import java.util.Map;

public class AbilityComponent implements ComponentV3, AutoSyncedComponent, ServerTickingComponent {
    private final PlayerEntity player;

    // DIE COMPOSITIONS
    private final AbilityInventoryComposition inventory = new AbilityInventoryComposition();
    private final AbilityCooldownComposition cooldowns = new AbilityCooldownComposition();

    public AbilityComponent(PlayerEntity player) {
        this.player = player;
    }

    public AbilityInventoryComposition getInventory() {
        return inventory;
    }

    public AbilityCooldownComposition getCooldowns() {
        return cooldowns;
    }

    @Override
    public void serverTick() {
        // --- GOMU GOMU NO GATLING SCHLAGHAGEL UPDATER ---
        GomuGatlingAbility.handleGatlingTick((ServerPlayerEntity) player);

        if (!player.getWorld().isClient && (!inventory.getGridInventory().isEmpty() || !cooldowns.getCooldownMap().isEmpty())) {
            cooldowns.tick();

            // Alle 5 Ticks synchronisieren, damit die Anzeige im HUD flüssig runterläuft
            if (player.age % 5 == 0) {
                Redline.ABILITY_COMPONENT.sync(player);
            }
        }
    }

    // --- ERWEITERTE NBT SPEICHERUNG INKLUSIVE COOLDOWNS ---
    @Override
    public void readFromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        // 1. Grid Inventar laden
        inventory.getGridInventory().clear();
        if (nbt.contains("jjk_grid_inventory")) {
            NbtCompound gridNbt = nbt.getCompound("jjk_grid_inventory");
            for (String key : gridNbt.getKeys()) {
                String abilityId = gridNbt.getString(key);
                inventory.getGridInventory().put(key, abilityId);
            }
        }

        // 2. Die 4 linken Keybind-Slots laden
        for (int i = 0; i < 4; i++) {
            if (nbt.contains("slot_" + i)) {
                inventory.getEquippedSlots()[i] = nbt.getString("slot_" + i);
            } else {
                inventory.getEquippedSlots()[i] = null;
            }
        }

        // 3. Die 8 rechten Scroll-Leisten-Slots laden
        for (int i = 0; i < 8; i++) {
            if (nbt.contains("scroll_slot_" + i)) {
                inventory.getScrollSlots()[i] = nbt.getString("scroll_slot_" + i);
            } else {
                inventory.getScrollSlots()[i] = null;
            }
        }

        // --- FIX 1: Lädt die Cooldown-Map sicher auf dem Client ein ---
        cooldowns.getCooldownMap().clear();
        if (nbt.contains("jjk_active_cooldowns")) {
            NbtCompound cooldownsNbt = nbt.getCompound("jjk_active_cooldowns");
            for (String key : cooldownsNbt.getKeys()) {
                cooldowns.getCooldownMap().put(key, cooldownsNbt.getInt(key));
            }
        }
    }

    @Override
    public void writeToNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        // 1. Grid Inventar speichern
        NbtCompound gridNbt = new NbtCompound();
        for (Map.Entry<String, String> entry : inventory.getGridInventory().entrySet()) {
            gridNbt.putString(entry.getKey(), entry.getValue());
        }
        nbt.put("jjk_grid_inventory", gridNbt);

        // 2. Die 4 linken Keybind-Slots speichern
        for (int i = 0; i < 4; i++) {
            String id = inventory.getEquippedSlots()[i];
            if (id != null) {
                nbt.putString("slot_" + i, id);
            }
        }

        // 3. Die 8 rechten Scroll-Leisten-Slots speichern
        for (int i = 0; i < 8; i++) {
            String id = inventory.getScrollSlots()[i];
            if (id != null) {
                nbt.putString("scroll_slot_" + i, id);
            }
        }

        // --- FIX 2: Schreibt die aktuellen Cooldown-Ticks fest in das Sync-Paket ---
        NbtCompound cooldownsNbt = new NbtCompound();
        for (Map.Entry<String, Integer> entry : cooldowns.getCooldownMap().entrySet()) {
            cooldownsNbt.putInt(entry.getKey(), entry.getValue());
        }
        nbt.put("jjk_active_cooldowns", cooldownsNbt);
    }
}