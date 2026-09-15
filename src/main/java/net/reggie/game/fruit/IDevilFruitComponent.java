package net.reggie.game.fruit;

import org.ladysnake.cca.api.v3.component.ComponentV3;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

public interface IDevilFruitComponent extends ComponentV3, AutoSyncedComponent {
    String getFruitId(); // Gibt z.B. "gomu_gomu" oder "none" zurück
    void setFruit(String fruitId);
    boolean hasFruit();
    void removeFruit();
}
