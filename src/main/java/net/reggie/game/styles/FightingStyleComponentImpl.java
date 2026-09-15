package net.reggie.game.styles;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.reggie.Redline;

public class FightingStyleComponentImpl implements IFightingStyleComponent {
    private final PlayerEntity player;
    private String styleId = "none";
    private long styleXp = 0;

    public FightingStyleComponentImpl(PlayerEntity player) {
        this.player = player;
    }

    @Override public String getStyleId() { return this.styleId; }
    @Override public boolean hasStyle() { return !this.styleId.equals("none"); }

    @Override
    public void setStyle(String id) {
        this.styleId = id == null ? "none" : id;
        Redline.FIGHTING_STYLE.sync(this.player); // Sofortiger Sync ans HUD
    }

    @Override public long getStyleXp() { return this.styleXp; }

    @Override
    public void setStyleXp(long xp) {
        this.styleXp = Math.max(0, xp);
        Redline.FIGHTING_STYLE.sync(this.player);
    }

    @Override
    public void addStyleXp(long amount) {
        setStyleXp(this.styleXp + amount);
    }

    @Override
    public float getStyleDamageBonus() {
        if (!hasStyle()) return 0f;
        // Beispiel: Sanft abflachendes Wachstum für den Linksklick-Bonus-Schaden
        return (float) (Math.sqrt(this.styleXp) * 0.15);
    }

    @Override
    public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registries) {
        this.styleId = tag.getString("ActiveFightingStyle");
        this.styleXp = tag.getLong("FightingStyleXp");
    }

    @Override
    public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registries) {
        tag.putString("ActiveFightingStyle", this.styleId);
        tag.putLong("FightingStyleXp", this.styleXp);
    }
}