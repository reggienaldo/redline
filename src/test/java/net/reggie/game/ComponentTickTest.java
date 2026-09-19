package net.reggie.game;

import net.minecraft.Bootstrap;
import net.minecraft.SharedConstants;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.reggie.Redline;
import net.reggie.game.abilities.AbilityComponent;
import net.reggie.game.component.CombatModeComponent;
import net.reggie.game.haki.HakiComponentImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ComponentTickTest {
    private ServerPlayerEntity player;
    private AbilityComponent ability;
    private CombatModeComponent combat;
    private HakiComponentImpl haki;

    @BeforeAll
    static void initializeComponentsWithoutStartingMinecraft() throws ClassNotFoundException {
        SharedConstants.createGameVersion();
        Bootstrap.initialize();
        try (MockedStatic<ComponentRegistry> registry = mockStatic(ComponentRegistry.class)) {
            registry.when(() -> ComponentRegistry.getOrCreate(any(Identifier.class), any()))
                    .thenAnswer(invocation -> mock(ComponentKey.class));
            Class.forName(Redline.class.getName());
        }
    }

    @BeforeEach
    void createPlayer() {
        reset(Redline.HAKI, Redline.ABILITY_COMPONENT, Redline.COMBAT_COMPONENT);
        player = mock(ServerPlayerEntity.class);
        ServerWorld world = mock(ServerWorld.class);
        when(player.getWorld()).thenReturn(world);
        when(player.getServerWorld()).thenReturn(world);
        player.age = 1;

        ability = new AbilityComponent(player);
        combat = new CombatModeComponent(player);
        haki = new HakiComponentImpl(player);
        when(Redline.ABILITY_COMPONENT.get(player)).thenReturn(ability);
        when(Redline.COMBAT_COMPONENT.get(player)).thenReturn(combat);
        when(Redline.HAKI.get(player)).thenReturn(haki);
    }

    @Test
    void bothServerTickPathsTogetherConsumeExactlyOneCooldownTick() {
        ability.getCooldowns().setCooldown("gomu_pistol", 80);
        for (int tick = 0; tick < 79; tick++) {
            haki.tick();
            ability.serverTick();
        }
        assertEquals(1, ability.getCooldowns().getRemainingTicks("gomu_pistol"));
        haki.tick();
        ability.serverTick();
        assertTrue(ability.getCooldowns().isReady("gomu_pistol"));
    }

    @Test
    void disablingCombatStopsHakiAndStillRegeneratesEnergy() {
        haki.setBusoUnlocked(true);
        haki.setBusoActive(true);
        haki.setHaki(25f);
        haki.tick();
        assertFalse(haki.isBusoActive());
        assertTrue(haki.getHaki() > 25f);
    }

    @Test
    void auraRequiresItsExistingMilestoneAndLosingItClearsEquippedSlots() {
        combat.setCombatModeEnabled(true);
        haki.setHaoUnlocked(true);
        haki.addHaoXp(3999);
        haki.tick();
        assertFalse(ability.getInventory().hasUnlocked("conq_aura"));

        haki.addHaoXp(1);
        haki.tick();
        assertTrue(ability.getInventory().hasUnlocked("conq_aura"));
        ability.getInventory().equipToSlot(0, "conq_aura", true);
        ability.getInventory().equipToSlot(1, "conq_aura", false);
        haki.setHaoActive(true);

        haki.addHaoXp(-1);
        haki.tick();
        assertFalse(haki.isHaoActive());
        assertFalse(ability.getInventory().hasUnlocked("conq_aura"));
        assertNull(ability.getInventory().getEquippedSlots()[0]);
        assertNull(ability.getInventory().getScrollSlots()[1]);
        assertFalse(ability.getCooldowns().isReady("conq_aura"));
    }

    @Test
    void revokingHakiStopsPreviouslyActivePowers() {
        combat.setCombatModeEnabled(true);
        haki.setBusoUnlocked(true);
        haki.setKenUnlocked(true);
        haki.setBusoActive(true);
        haki.setKenActive(true);
        haki.setBusoUnlocked(false);
        haki.setKenUnlocked(false);
        haki.tick();
        assertFalse(haki.isBusoActive());
        assertFalse(haki.isKenActive());
    }
}
