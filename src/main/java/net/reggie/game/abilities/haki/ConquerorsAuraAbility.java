package net.reggie.game.abilities.haki;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.reggie.Redline;
import net.reggie.game.abilities.IAbility;
import net.reggie.game.haki.IHakiComponent;
import net.reggie.particle.ModParticles;
import net.reggie.sound.ModSounds;

import java.util.List;

public class ConquerorsAuraAbility implements IAbility {

    @Override public String getId() { return "conq_aura"; }

    @Override
    public String getName() {
        return Text.literal("Königshaki Aura").formatted(Formatting.DARK_RED, Formatting.BOLD).getString();
    }

    @Override
    public String getDescription() {
        return "Schaltet deine Königshaki-Aura ein, die kontinuierlich Haki verbraucht.";
    }

    @Override public boolean isPassive() { return false; }

    @Override
    public int getCooldownTicks() {
        return 200; // Deklaration für das System (10 Sekunden)
    }

    @Override
    public Identifier getIconTexture() {
        return Identifier.of(Redline.MOD_ID, "textures/abilities/conquerors_aura.png");
    }

    @Override
    public void execute(ServerPlayerEntity player) {
        IHakiComponent haki = Redline.HAKI.get(player);
        var abilityComp = Redline.ABILITY_COMPONENT.get(player);

        if (!haki.hasConquerorAura()) {
            player.sendMessage(Text.literal("Du besitzt kein Königshaki, um diese Aura zu nutzen!").formatted(Formatting.RED), true);
            return;
        }

        boolean newState = !haki.isHaoActive();

        // --- TICK-BASIERTER COOLDOWN-CHECK VOR DEM EINSCHALTEN ---
        if (newState && !abilityComp.getCooldowns().isReady("conq_aura")) {
            player.sendMessage(Text.literal("§cDie Königshaki-Aura hat noch Cooldown!"), true);
            return;
        }

        if (newState && !player.isCreative() && Redline.DEVIL_FRUIT.get(player).hasFruit() && player.isTouchingWater()) {
            player.sendMessage(Text.literal("§cDas Wasser lähmt deinen Körper!"), true);
            return;
        }

        float cost = this.getCost();
        if (newState && !haki.canUseHaki(cost)) {
            player.sendMessage(Text.literal("Nicht genug Haki-Energie zum Starten!"), true);
            return;
        }

        if (newState) {
            haki.consumeHaki(cost);
        }
        haki.setHaoActive(newState);

        // --- COOLDOWN APPLIEN BEI MANUELLEM AUSSCHALTEN ---
        if (!newState) {
            abilityComp.getCooldowns().setCooldown("conq_aura", 200); // Auf einheitliche 10 Sekunden (200 Ticks) korrigiert
        }

        // --- ENTSCHEIDENDER SYNC-FIX FÜR DEN CLIENT-THREAD ---
        // Zwingt Cardinal Components dazu, den neuen An/Aus-Zustand des Hakis SOFORT an die Grafikkarte zu funken!
        Redline.HAKI.sync(player);
        Redline.ABILITY_COMPONENT.sync(player);
    }

    // Flag für das veränderte ModNetworking, damit dort kein automatischer Cooldown gesetzt wird
    @Override
    public boolean isToggleable() {
        return true;
    }

    @Override
    public float getCost() {
        return 50f;
    }
}