package net.reggie.game;

import net.minecraft.nbt.NbtCompound;
import net.reggie.game.abilities.AbilityComponent;
import net.reggie.game.fruit.DevilFruitComponentImpl;
import net.reggie.game.styles.FightingStyleComponentImpl;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SavedComponentTest {
    @Test
    void absentAndEmptySaveTagsDoNotGrantFruitOrStyleOwnership() {
        var fruit = new DevilFruitComponentImpl(null);
        var style = new FightingStyleComponentImpl(null);
        NbtCompound tag = new NbtCompound();

        fruit.readFromNbt(tag, null);
        style.readFromNbt(tag, null);
        assertFalse(fruit.hasFruit());
        assertFalse(style.hasStyle());

        tag.putString("ActiveDevilFruit", "");
        tag.putString("ActiveFightingStyle", "");
        fruit.readFromNbt(tag, null);
        style.readFromNbt(tag, null);
        assertFalse(fruit.hasFruit());
        assertFalse(style.hasStyle());

        tag.putString("ActiveDevilFruit", "gomu_gomu");
        tag.putString("ActiveFightingStyle", "black_leg");
        fruit.readFromNbt(tag, null);
        style.readFromNbt(tag, null);
        assertEquals("gomu_gomu", fruit.getFruitId());
        assertEquals("black_leg", style.getStyleId());
    }

    @Test
    void loadingAnotherPlayersSaveCannotReplaceOrPersistRunningGatlingState() {
        var first = new AbilityComponent(null);
        var second = new AbilityComponent(null);
        assertNotSame(first.getGatling(), second.getGatling());

        first.getInventory().unlockAbilityDynamically("gomu_gatling");
        first.getInventory().equipToSlot(0, "gomu_gatling", true);
        first.getCooldowns().setCooldown("gomu_gatling", 240);
        NbtCompound tag = new NbtCompound();
        first.writeToNbt(tag, null);
        second.readFromNbt(tag, null);

        assertEquals("gomu_gatling", second.getInventory().getEquippedSlots()[0]);
        assertEquals(240, second.getCooldowns().getRemainingTicks("gomu_gatling"));
        assertFalse(second.getGatling().isChannelling());
        assertNotSame(first.getGatling(), second.getGatling());
    }
}
