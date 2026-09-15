package net.reggie.game.abilities;

import net.minecraft.util.Identifier;
import net.reggie.game.abilities.fruit.gomu.GomuBazookaAbility;
import net.reggie.game.abilities.fruit.gomu.GomuGatlingAbility;
import net.reggie.game.abilities.fruit.gomu.GomuPistolAbility;
import net.reggie.game.abilities.haki.ConquerorsAuraAbility;

import java.util.HashMap;
import java.util.Map;

public class ModAbilities {
    private static final Map<String, IAbility> REGISTRY = new HashMap<>();

    // PASSIVE PERKS

    //Devil Fruit Abilities
    public static final IAbility GOMU_PISTOL = register(new GomuPistolAbility());
    public static final IAbility GOMU_BAZOOKA = register(new GomuBazookaAbility());
    public static final IAbility GOMU_GATLING = register(new GomuGatlingAbility());

    // NEU: HIER WIRD DIE ECHTE KÖNIGSHAKI AURA REGISTRIERT!
    public static final IAbility CONQ_AURA = register(new ConquerorsAuraAbility());

    private static IAbility register(IAbility ability) {
        REGISTRY.put(ability.getId(), ability);
        return ability;
    }

    public static IAbility get(String id) {
        return REGISTRY.get(id);
    }

    private record SimpleAbility(String id, String name, String description, boolean isPassive) implements IAbility {
        @Override public String getId() { return id; }
        @Override public String getName() { return name; }
        @Override public String getDescription() { return description; }
        @Override public boolean isPassive() { return isPassive; }
        @Override public void execute(net.minecraft.server.network.ServerPlayerEntity player) {} // Passiv hat keine aktive Ausführung
        @Override public int getCooldownTicks() { return 0; }
        @Override
        public Identifier getIconTexture() {
            return Identifier.of("redline", "textures/gui/icons/default_passive.png");
        }
    }
}
