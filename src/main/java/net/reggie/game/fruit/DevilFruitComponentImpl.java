package net.reggie.game.fruit;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.reggie.Redline;

public class DevilFruitComponentImpl implements IDevilFruitComponent {
    private final PlayerEntity player;
    private String fruitId = "none";

    public DevilFruitComponentImpl(PlayerEntity player) {
        this.player = player;
    }

    @Override public String getFruitId() { return this.fruitId; }

    @Override
    public boolean hasFruit() { return !this.fruitId.equals("none"); }

    @Override
    public void setFruit(String id) {
        this.fruitId = id == null ? "none" : id;
        Redline.DEVIL_FRUIT.sync(this.player); // Sofortiger Client-Sync
    }

    @Override
    public void removeFruit() {
        setFruit("none");
    }

    @Override
    public void readFromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registries) {
        this.fruitId = tag.getString("ActiveDevilFruit");
    }

    @Override
    public void writeToNbt(NbtCompound tag, RegistryWrapper.WrapperLookup registries) {
        tag.putString("ActiveDevilFruit", this.fruitId);
    }
}
