package net.reggie.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.reggie.Redline;
import net.reggie.game.abilities.IAbility;
import net.reggie.game.abilities.ModAbilities;

public class    AbilityHudRenderer implements HudRenderCallback {

    private static final Identifier ABILITY_HUD = Identifier.of(Redline.MOD_ID, "textures/gui/ability_hud/abilityhud.png");
    private static final Identifier ABILITY_HUD_POINTER = Identifier.of(Redline.MOD_ID, "textures/gui/ability_hud/abilityhud_pointer.png");
    private static final Identifier ABILITY_HUD_KEY = Identifier.of(Redline.MOD_ID, "textures/gui/ability_hud/abilitykeyhud.png");
    private static final Identifier CD_TEXTURE = Identifier.of(Redline.MOD_ID, "textures/gui/ability_hud/cd.png");
    private static final Identifier EMPTY_SLOT = Identifier.of(Redline.MOD_ID, "textures/gui/ability_hud/empty_slot.png");

    private static final int TEXTURE_WIDTH = 22;
    private static final int TEXTURE_HEIGHT = 186;

    public static int abilityHudPointer = 0;

    // --- ALARM-SYSTEM ---
    public static int alertDisplayTicks = 0;
    public static String alertAbilityId = null;

    @Override
    public void onHudRender(DrawContext drawContext, RenderTickCounter renderTickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || client.options.hudHidden) return;

        boolean combatActive = Redline.COMBAT_COMPONENT.get(client.player).isCombatModeEnabled();
        if (!combatActive) return;

        var abilityComp = Redline.ABILITY_COMPONENT.get(client.player);
        String[] equipped = abilityComp.getInventory().getEquippedSlots();
        String[] scrolled = abilityComp.getInventory().getScrollSlots();

        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        int rightX = screenWidth - TEXTURE_WIDTH;
        int rightY = screenHeight - TEXTURE_HEIGHT;

        // =========================================================
        // DIE PERFEKTE ACTIONBAR- & ALARM-STAPELUNG (STATUS FIX)
        // =========================================================
        int centerX = screenWidth / 2;
        int actionbarY = screenHeight - 68;

        boolean isAlertActive = (alertDisplayTicks > 0 && alertAbilityId != null);

        // 1. SCHRITT: Wenn ein Alarm aktiv ist (Tastendruck während Cooldown)
        if (isAlertActive) {
            IAbility alertAbility = ModAbilities.get(alertAbilityId);
            if (alertAbility != null) {
                int remainingTicks = abilityComp.getCooldowns().getRemainingTicks(alertAbilityId);
                int displaySecs = remainingTicks > 0 ? (remainingTicks / 20) + 1 : 10;

                drawContext.getMatrices().push();
                drawContext.getMatrices().translate(0, 0, 400.0F);

                String topWarning = "§cThat ability is on cooldown!";
                drawContext.drawCenteredTextWithShadow(client.textRenderer, topWarning, centerX, actionbarY - 12, 16777215);

                drawContext.getMatrices().pop();

                // Berechnet das Status-Feedback für die Fehlermeldung
                String bottomStatus;
                if (alertAbility.isToggleable()) {
                    var hakiComp = Redline.HAKI.get(client.player); // Nutzt deinen echten Key
                    // Holt den exakten Live-Zustand vom Client-Objekt
                    String stateText = hakiComp.isHaoActive() ? "§aON" : "§cOFF";
                    bottomStatus = "§f" + alertAbility.getName().toUpperCase() + " §7| " + stateText;
                } else {
                    bottomStatus = "§f" + alertAbility.getName().toUpperCase() + " §7| §c" + displaySecs + "s";
                }

                client.player.sendMessage(Text.literal(bottomStatus), true);

                alertDisplayTicks--;
                if (alertDisplayTicks <= 0) {
                    alertAbilityId = null;
                }
            }
        }

        // STANDARD-ZUSTAND: Wenn kein Alarm brennt, läuft die ganz normale Scroll-Anzeige
        if (!isAlertActive && abilityHudPointer >= 0 && abilityHudPointer < scrolled.length) {
            String abilityId = scrolled[abilityHudPointer];
            if (abilityId != null) {
                IAbility ability = ModAbilities.get(abilityId);
                if (ability != null) {
                    int remainingTicks = abilityComp.getCooldowns().getRemainingTicks(abilityId);
                    String hudMessage;

                    // Wenn es sich um das umschaltbare Königshaki handelt
                    if (ability.isToggleable()) {
                        var hakiComp = Redline.HAKI.get(client.player);

                        // Wenn der Cooldown noch läuft UND die Aura aus ist, zeigen wir OFF
                        if (remainingTicks > 0 && !hakiComp.isHaoActive()) {
                            hudMessage = "§f" + ability.getName().toUpperCase() + " §7| §cOFF";
                        }
                        // Wenn die Aura brennt (isHaoActive = true), zeigt das HUD stolz ON in grün!
                        else if (hakiComp.isHaoActive()) {
                            hudMessage = "§f" + ability.getName().toUpperCase() + " §7| §aON";
                        }
                        // Wenn sie aus ist und kein Cooldown läuft, zeigt sie ganz normal ihre Haki-Kosten
                        else {
                            int hakiCost = (int) ability.getCost();
                            hudMessage = "§f" + ability.getName().toUpperCase() + " §7| §b" + hakiCost;
                        }
                    } else {
                        // Normale Fähigkeiten behalten ihre Sekunden oder Kosten
                        if (remainingTicks > 0) {
                            int remainingSecs = (remainingTicks / 20) + 1;
                            hudMessage = "§f" + ability.getName().toUpperCase() + " §7| §c" + remainingSecs + "s";
                        } else {
                            int hakiCost = (int) ability.getCost();
                            hudMessage = "§f" + ability.getName().toUpperCase() + " §7| §b" + hakiCost;
                        }
                    }

                    client.player.sendMessage(Text.literal(hudMessage), true);
                }
            } else {
                client.player.sendMessage(Text.literal(""), true);
            }
        }

        // =========================================================
        // SCHICHT 1: ALLE TEXTUREN UND RAHMEN RENDERN
        // =========================================================
        drawContext.getMatrices().push();
        drawContext.getMatrices().translate(rightX, rightY, 0.0F);
        int offset = 20;
        for (int i = 0; i < 8; ++i) {
            Identifier iconImage = EMPTY_SLOT;
            String scrId = scrolled[i];

            if (scrId != null) {
                IAbility ability = ModAbilities.get(scrId);
                if (ability != null) iconImage = ability.getIconTexture();
            }

            RenderSystem.enableBlend();
            drawContext.drawTexture(iconImage, 3, offset, 0.0F, 0.0F, 16, 16, 16, 16);

            if (scrId != null && abilityComp.getCooldowns().getRemainingTicks(scrId) > 0) {
                drawContext.drawTexture(CD_TEXTURE, 3, offset, 0.0F, 0.0F, 16, 16, 16, 16);
            }
            offset += 21;
        }
        drawContext.drawTexture(ABILITY_HUD, 0, 0, 0.0F, 0.0F, TEXTURE_WIDTH, TEXTURE_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        offset = 17;
        for (int i = 0; i < 8; ++i) {
            if (i == abilityHudPointer) {
                drawContext.drawTexture(ABILITY_HUD_POINTER, 0, offset, 0.0F, 0.0F, 22, 22, 22, 22);
            }
            offset += 21;
        }
        drawContext.getMatrices().pop();

        // Linke horizontale Leiste
        int leftX = 1;
        int leftY = screenHeight - 36;
        drawContext.getMatrices().push();
        drawContext.getMatrices().translate(leftX, leftY, 0.0F);
        int iconHeightOffset = 16;
        int[] slotXPositions = {4, 27, 51, 73};
        for (int i = 0; i < 4; ++i) {
            Identifier iconImage = EMPTY_SLOT;
            String eqId = equipped[i];

            if (eqId != null) {
                IAbility ability = ModAbilities.get(eqId);
                if (ability != null) iconImage = ability.getIconTexture();
            }

            int currentSlotX = slotXPositions[i];
            drawContext.drawTexture(iconImage, currentSlotX, iconHeightOffset, 0.0F, 0.0F, 16, 16, 16, 16);

            if (eqId != null && abilityComp.getCooldowns().getRemainingTicks(eqId) > 0) {
                drawContext.drawTexture(CD_TEXTURE, currentSlotX, iconHeightOffset, 0.0F, 0.0F, 16, 16, 16, 16);
            }
        }
        drawContext.drawTexture(ABILITY_HUD_KEY, 0, 0, 0.0F, 0.0F, 94, 36, 94, 36);
        drawContext.getMatrices().pop();

        // =========================================================
        // SCHICHT 2: ABSOLUTES VORDERGRUND TEXT-RENDERING ON SLOTS
        // =========================================================
        int textOffset = rightY + 20;
        for (int i = 0; i < 8; ++i) {
            String scrId = scrolled[i];
            if (scrId != null) {
                int remainingTicks = abilityComp.getCooldowns().getRemainingTicks(scrId);
                if (remainingTicks > 0) {
                    int remainingSeconds = (remainingTicks / 20) + 1;

                    drawContext.getMatrices().push();
                    drawContext.getMatrices().translate(0, 0, 400.0F);
                    drawContext.drawCenteredTextWithShadow(
                            client.textRenderer,
                            String.valueOf(remainingSeconds),
                            rightX + 3 + 8,
                            textOffset + 4,
                            16777215
                    );
                    drawContext.getMatrices().pop();
                }
            }
            textOffset += 21;
        }

        for (int i = 0; i < 4; ++i) {
            String eqId = equipped[i];
            if (eqId != null) {
                int remainingTicks = abilityComp.getCooldowns().getRemainingTicks(eqId);
                if (remainingTicks > 0) {
                    int remainingSeconds = (remainingTicks / 20) + 1;

                    drawContext.getMatrices().push();
                    drawContext.getMatrices().translate(0, 0, 400.0F);
                    drawContext.drawCenteredTextWithShadow(
                            client.textRenderer,
                            String.valueOf(remainingSeconds),
                            leftX + slotXPositions[i] + 8,
                            leftY + iconHeightOffset + 4,
                            16777215
                    );
                    drawContext.getMatrices().pop();
                }
            }
        }
    }
}