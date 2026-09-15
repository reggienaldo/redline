package net.reggie.game.races;

import org.ladysnake.cca.api.v3.component.ComponentV3;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

public interface IRaceComponent extends ComponentV3, AutoSyncedComponent {
    String getRaceId(); // Gibt z.B. "human", "fishman", "mink", "skypiean" zurück
    void setRace(String raceId);

    boolean isHuman();
    boolean isFishman();
    boolean isMink();
    boolean isSkypiean();
    boolean isOni();      // --- NEU ---
    boolean isLunarian();
}