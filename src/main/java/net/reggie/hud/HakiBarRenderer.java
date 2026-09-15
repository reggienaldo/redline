package net.reggie.hud;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;
import net.reggie.Redline;
import net.reggie.game.haki.IHakiComponent;

import java.awt.*;

public class HakiBarRenderer implements HudRenderCallback {

    public static final Color FULL =
            new Color(20, 20, 20, 220); // Klassisch Schwarz für Rüstungshaki

    public static final Color COATING_COLOR =
            new Color(139, 0, 0, 240); // Dunkelrot/Crimson für aktives Königshaki-Coating

    public static final Color KEN_COLOR =
            new Color(0, 191, 255, 220); // GPO-Blau/Cyan für aktives Beobachtungshaki

    public static final Color EMPTY =
            new Color(60, 60, 60, 180); // Dunkelgrau Hintergrund

    private static final int WIDTH = 100;
    private static final int HEIGHT = 10;
    private static final int OUTLINE_THICKNESS = 1;

    private static final Color OUTLINE = Color.WHITE;

    @Override
    public void onHudRender(DrawContext drawContext, RenderTickCounter renderTickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        // Combat Mode Check über deine existierende Kampf-Komponente
        boolean combatActive = Redline.COMBAT_COMPONENT
                .get(client.player)
                .isCombatModeEnabled();

        if (!combatActive) return;

        // Holt deine flache Haki-Komponente über den zentralen Redline-Key
        IHakiComponent hakiComp = Redline.HAKI.get(client.player);

        // --- MODIFIZIERTER CHECK: Zeigt die Bar an, wenn mindestens eine Haki-Art UNLOCKED ist ---
        boolean hasAnyHakiGelernt = hakiComp.isBusoUnlocked() || hakiComp.isKenUnlocked() || hakiComp.isHaoUnlocked();
        if (!hasAnyHakiGelernt) return;

        float haki = hakiComp.getHaki();
        float maxHaki = hakiComp.getMaxHaki(); // Nutzt das bombenfeste Level-Skalierungssystem!

        int margin = 12;
        int x = client.getWindow().getScaledWidth() - WIDTH - margin;
        int y = margin;

        // =========================
        // OUTLINE (Umrandung)
        // =========================
        drawContext.fill(
                x - OUTLINE_THICKNESS,
                y - OUTLINE_THICKNESS,
                x + WIDTH + OUTLINE_THICKNESS,
                y + HEIGHT + OUTLINE_THICKNESS,
                OUTLINE.getRGB()
        );

        // =========================
        // EMPTY BAR (Hintergrund)
        // =========================
        drawContext.fill(
                x,
                y,
                x + WIDTH,
                y + HEIGHT,
                EMPTY.getRGB()
        );

        // =========================
        // FILLED BAR (Füllung)
        // =========================
        float percentage = maxHaki > 0 ? (haki / maxHaki) : 0f;
        int filled = (int) (MathHelper.clamp(percentage, 0f, 1f) * WIDTH);

        // Standardfarbe ist Rüstungshaki schwarz
        int barColor = FULL.getRGB();

        // DYNAMISCHES FARB-FEEDBACK (Liest die echten Zustände der flachen Komponente aus)
        if (hakiComp.hasConquerorCoating() && hakiComp.isBusoActive()) {
            // Wenn Königshaki-Coating freigeschaltet und Rüstungshaki an ist, färbt sich die Leiste Crimson-Rot
            barColor = COATING_COLOR.getRGB();
        } else if (hakiComp.isKenActive()) {
            // Wenn Observationshaki aktiv ist, färbt sie sich Cyan-Blau
            barColor = KEN_COLOR.getRGB();
        }

        drawContext.fill(
                x,
                y,
                x + filled,
                y + HEIGHT,
                barColor
        );

        // =========================
        // TEXT (Präzise Werteanzeige)
        // =========================
        String label = "Haki (" + (int)haki + "/" + (int)maxHaki + ")";

        drawContext.drawCenteredTextWithShadow(
                client.textRenderer,
                Text.literal(label),
                x + WIDTH / 2,
                y + HEIGHT / 2 - 4,
                Color.WHITE.getRGB()
        );
    }
}