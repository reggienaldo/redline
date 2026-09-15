package net.reggie.game.abilities;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public interface IAbility {
    String getId();          // z.B. "fire_fist", "ryou"
    String getName();        // Anzeige-Name im GUI
    String getDescription(); // Beschreibungstext
    boolean isPassive();     // true = Passiver Perk, false = Aktiver Angriff (ausrüstbar)

    void execute(ServerPlayerEntity player);
    int getCooldownTicks();
    Identifier getIconTexture();

    default boolean isToggleable() {
        return false;
    }

    default float getCost() {
        return 0f;
    }
}
