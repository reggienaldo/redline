package net.reggie.game.doriki;

import net.minecraft.util.Identifier;
import net.reggie.Redline;
import org.ladysnake.cca.api.v3.component.ComponentV3;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

public interface IDorikiComponent extends ComponentV3, AutoSyncedComponent {
    // Feste Identifier für deine Doriki-Modifier in 1.21.1
    Identifier HEALTH_MODIFIER_ID = Identifier.of(Redline.MOD_ID, "doriki_health_bonus");
    Identifier DAMAGE_MODIFIER_ID = Identifier.of(Redline.MOD_ID, "doriki_damage_bonus");

    long getDoriki();
    void setDoriki(long value);
    void addDoriki(long amount);

    // Auf double angepasst für native Attribut-Kompatibilität
    double getHealthBonus();
    double getDamageBonus();

}
