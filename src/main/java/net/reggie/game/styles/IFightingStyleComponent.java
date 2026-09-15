package net.reggie.game.styles;

import org.ladysnake.cca.api.v3.component.ComponentV3;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

public interface IFightingStyleComponent extends ComponentV3, AutoSyncedComponent {
    String getStyleId(); // Gibt z.B. "black_leg" oder "none" zurück
    void setStyle(String styleId);
    boolean hasStyle();

    long getStyleXp();
    void setStyleXp(long xp);
    void addStyleXp(long amount);

    // Berechnet den Bonus-Nahkampfschaden basierend auf dem Kampfstil-Fortschritt
    float getStyleDamageBonus();
}
