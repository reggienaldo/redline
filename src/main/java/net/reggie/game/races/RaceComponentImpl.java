package net.reggie.game.races;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.reggie.Redline;

public class RaceComponentImpl implements IRaceComponent {
    private final PlayerEntity player;
    private String raceId = "human"; // Standardmäßig ist jeder Spieler ein Mensch

    public RaceComponentImpl(PlayerEntity player) {
        this.player = player;
    }

    @Override public String getRaceId() { return this.raceId; }

    @Override
    public void setRace(String id) {
        this.raceId = id == null ? "human" : id;
        Redline.RACE.sync(this.player); // Sofortiger Client-Sync
    }

    @Override public boolean isHuman() { return this.raceId.equals("human"); }
    @Override public boolean isFishman() { return this.raceId.equals("fishman"); }
    @Override public boolean isMink() { return this.raceId.equals("mink"); }
    @Override public boolean isSkypiean() { return this.raceId.equals("skypiean"); }
    @Override public boolean isOni() { return this.raceId.equals("oni"); }
    @Override public boolean isLunarian() { return this.raceId.equals("lunarian"); }

    @Override
    public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registries) {
        if (tag.contains("PlayerRace")) {
            this.raceId = tag.getString("PlayerRace");
        } else {
            this.raceId = "human";
        }
    }

    @Override
    public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registries) {
        tag.putString("PlayerRace", this.raceId);
    }
}
