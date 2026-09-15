package net.reggie.game.haki;

import org.ladysnake.cca.api.v3.component.ComponentV3;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;

public interface IHakiComponent extends ComponentV3, AutoSyncedComponent {
    float getHaki();
    void setHaki(float value);
    void addHaki(float amount);
    void consumeHaki(float amount);
    float getMaxHaki();
    float getHakiRegenRate();
    boolean canUseHaki(float amount);
    long getTotalXp();
    void tick();

    // --- Busoshoku (Rüstung) ---
    boolean isBusoUnlocked();             // NEU: Überprüft, ob gelernt
    void setBusoUnlocked(boolean unlocked); // NEU: Schaltet die Fähigkeit frei
    boolean isBusoActive();
    void setBusoActive(boolean active);
    long getBusoXp();
    void addBusoXp(long amount);
    int getBusoLevel();
    boolean isRyouUnlocked();
    boolean isEmissionUnlocked();
    boolean isInternalDestructionUnlocked();

    // --- Kenbunshoku (Beobachtung) ---
    boolean isKenUnlocked();             // NEU: Überprüft, ob gelernt
    void setKenUnlocked(boolean unlocked); // NEU: Schaltet die Fähigkeit frei
    boolean isKenActive();
    void setKenActive(boolean active);
    long getKenXp();
    void addKenXp(long amount);
    int getKenLevel();
    boolean isAdvancedObservationUnlocked();
    boolean isFutureSightUnlocked();

    // --- Haoshoku (Königshaki) ---
    boolean isHaoUnlocked();
    void setHaoUnlocked(boolean unlocked);
    long getHaoXp();
    void addHaoXp(long amount);
    int getHaoLevel();
    boolean hasConquerorAura();
    boolean hasConquerorCoating();

    // --- Mastery Brücken für alte Client-Dateien ---
    default int getBusoshokuMastery() { return isBusoUnlocked() ? getBusoLevel() : 0; }
    default int getKenbunshokuMastery() { return isKenUnlocked() ? getKenLevel() : 0; }
    int getRyouColorIndex();
    void setRyouColorIndex(int index);

    boolean isHaoActive();
    void setHaoActive(boolean active);

    int getAuraCdDisplayTicks();
    void showAuraCdDisplay(int ticks);
}
