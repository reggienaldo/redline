package net.reggie.game.component;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.reggie.Redline;
import org.ladysnake.cca.api.v3.component.ComponentV3;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

public class CombatModeComponent implements ComponentV3, AutoSyncedComponent {

    private boolean combatModeEnabled = false;
    private final PlayerEntity player;

    public CombatModeComponent(PlayerEntity player) {
        this.player = player;
    }

    public boolean isCombatModeEnabled() {
        return combatModeEnabled;
    }

    public void toggleCombatMode() {
        combatModeEnabled = !combatModeEnabled;
        Redline.COMBAT_COMPONENT.sync(player);
    }

    public void setCombatModeEnabled(boolean enabled) {
        combatModeEnabled = enabled;
        Redline.COMBAT_COMPONENT.sync(player);
    }

    @Override
    public void readFromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        combatModeEnabled = nbt.getBoolean("combatModeEnabled");
    }

    @Override
    public void writeToNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        nbt.putBoolean("combatModeEnabled", combatModeEnabled);
    }
}
