package net.reggie.gui;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.reggie.Redline;
import net.reggie.game.abilities.AbilityComponent;
import net.reggie.game.abilities.IAbility;
import net.reggie.game.abilities.ModAbilities;
import net.reggie.network.C2S.EquipAbilityC2SPayload;

import java.awt.*;
import java.util.List;


public class TechniqueInventoryScreen extends Screen {
    // --- TEXTUREN PFADE ---
    private static final Identifier BACKGROUND_TEXTURE = Identifier.of(Redline.MOD_ID, "textures/gui/ability_inventory/ability_inventory.png");
    private static final Identifier SLOT_TEXTURE = Identifier.of(Redline.MOD_ID, "textures/gui/ability_inventory/slot.png");
    private static final Identifier SELECTED_SLOT_TEXTURE = Identifier.of(Redline.MOD_ID, "textures/gui/ability_inventory/slot_selected.png");
    private static final Identifier DELETE_TEXTURE = Identifier.of(Redline.MOD_ID, "textures/gui/ability_inventory/x_button.png");
    private static final Identifier INVENTORY_TEXTURE = Identifier.of(Redline.MOD_ID, "textures/gui/ability_inventory/ability_slots.png");

    private final AbilityComponent abilityComp;

    // Auswahl-Zustände aus dem Originalcode
    private int selectedSlot = 0;
    private boolean isKeybindButtonPressed = true; // true = links (Keybinds), false = rechts (Scroll-Slots)
    private String hoveredAbilityId = null;

    private int boardX, boardY;
    private int gridX, gridY;

    public TechniqueInventoryScreen(AbilityComponent abilityComp) {
        super(Text.literal("Technique Inventory"));
        this.abilityComp = abilityComp;
    }

    @Override
    protected void init() {
        this.clearChildren();
        this.boardX = (this.width - 295) / 2;
        this.boardY = (this.height - 214) / 2;
        this.gridX = this.width / 2 - 87;
        this.gridY = this.height / 2 - 77;
    }

    @Override
    public void render(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        drawContext.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        // 1. Board Hintergrund
        drawContext.drawTexture(BACKGROUND_TEXTURE, boardX, boardY, 0.0F, 0.0F, 295, 214, 295, 214);

        // 2. Mittleres Gitter
        drawContext.drawTexture(INVENTORY_TEXTURE, gridX, gridY, 0.0F, 0.0F, 134, 172, 134, 172);

        this.hoveredAbilityId = null;
        int slotSpacing = 22;

        String[] equipped = abilityComp.getInventory().getEquippedSlots();
        String[] scrolled = abilityComp.getInventory().getScrollSlots();

        // =========================================================
        // 3. RECHTE REIHEN-SLOTS (0-7)
        // =========================================================
        int rightSlotsStartX = this.width / 2 + 58;
        int rightSlotsStartHeight = this.height / 2 - 78;

        for (int i = 0; i < 8; ++i) {
            int slotY = rightSlotsStartHeight + slotSpacing * i;
            String scrId = scrolled[i];

            drawContext.drawTexture(SLOT_TEXTURE, rightSlotsStartX, slotY, 0.0F, 0.0F, 22, 22, 22, 22);
            drawContext.drawTexture(DELETE_TEXTURE, rightSlotsStartX + 22, slotY + 6, 0.0F, 0.0F, 9, 10, 9, 10);

            if (scrId != null) {
                IAbility ability = ModAbilities.get(scrId);
                if (ability != null) {
                    drawContext.drawTexture(ability.getIconTexture(), rightSlotsStartX + 3, slotY + 3, 0.0F, 0.0F, 16, 16, 16, 16);
                }
            }

            if (this.selectedSlot == i && !this.isKeybindButtonPressed) {
                drawContext.drawTexture(SELECTED_SLOT_TEXTURE, rightSlotsStartX, slotY, 0.0F, 0.0F, 22, 22, 22, 22);
            }
        }

        // =========================================================
        // 4. LINKE KEYBIND-SLOTS (0-3)
        // =========================================================
        int keybindStartX = this.width / 2 - 131;
        int keybindStartHeight = this.height / 2 + 8;

        for (int i = 0; i < 4; ++i) {
            int slotY = keybindStartHeight + slotSpacing * i;
            String eqId = equipped[i];

            drawContext.drawTexture(SLOT_TEXTURE, keybindStartX, slotY, 0.0F, 0.0F, 22, 22, 22, 22);
            drawContext.drawTexture(DELETE_TEXTURE, keybindStartX + 22, slotY + 6, 0.0F, 0.0F, 9, 10, 9, 10);

            if (eqId != null) {
                IAbility ability = ModAbilities.get(eqId);
                if (ability != null) {
                    drawContext.drawTexture(ability.getIconTexture(), keybindStartX + 3, slotY + 3, 0.0F, 0.0F, 16, 16, 16, 16);
                }
            }

            if (this.selectedSlot == i && this.isKeybindButtonPressed) {
                drawContext.drawTexture(SELECTED_SLOT_TEXTURE, keybindStartX, slotY, 0.0F, 0.0F, 22, 22, 22, 22);
            }
        }

        // =========================================================
        // 5. MITTLERES RASTER (DYNAMISCHES NACHRUTSCHEN)
        // =========================================================
        int abilityStartX = gridX + 2;
        int abilityStartHeight = gridY + 2;

        int currentWidth = abilityStartX;
        int currentHeight = abilityStartHeight;
        int currentSlot = 0;

        List<String> unlockedList = abilityComp.getInventory().getGridInventory().values().stream()
                .map(obj -> (String) obj)
                .toList();

        for (int i = 0; i < unlockedList.size(); ++i) {
            String abilityId = unlockedList.get(i);

            // Filter 1: Wenn die Fähigkeit nicht gelernt wurde, komplett überspringen!
            if (!abilityComp.getInventory().hasUnlocked(abilityId)) continue;

            // Filter 2: Ausblenden, wenn links oder rechts ausgerüstet
            boolean isEquipped = false;
            for (String eq : equipped) { if (abilityId.equals(eq)) isEquipped = true; }
            for (String scr : scrolled) { if (abilityId.equals(scr)) isEquipped = true; }
            if (isEquipped) continue;

            IAbility ability = ModAbilities.get(abilityId);

            if (ability != null) {
                int maxButtonsEachRow = 7;
                // JJK-Nachrutsch-Formel: Berechnet die Reihe basierend auf dem fortlaufenden currentSlot!
                if (currentSlot % maxButtonsEachRow == 0 && currentSlot > 0) {
                    currentHeight += 19;
                    currentWidth = abilityStartX;
                }

                // Zeichnet das Icon an die nachgerutschte Position
                drawContext.drawTexture(ability.getIconTexture(), currentWidth, currentHeight, 0.0F, 0.0F, 16, 16, 16, 16);

                if (mouseX >= currentWidth - 1 && mouseX < currentWidth + 17 && mouseY >= currentHeight - 1 && mouseY < currentHeight + 17) {
                    this.hoveredAbilityId = abilityId;
                    drawContext.fill(currentWidth - 1, currentHeight - 1, currentWidth + 17, currentHeight + 17, new Color(255, 255, 255, 50).getRGB());
                }

                ++currentSlot;
                currentWidth += 19;
            }
        }

        drawContext.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        super.render(drawContext, mouseX, mouseY, delta);

        if (this.hoveredAbilityId != null) {
            IAbility hability = ModAbilities.get(this.hoveredAbilityId);
            if (hability != null) {
                drawContext.drawTooltip(this.textRenderer, Text.literal(hability.getName() + "\n§7" + hability.getDescription()), mouseX, mouseY);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int slotSpacing = 22;
        String[] equipped = abilityComp.getInventory().getEquippedSlots();
        String[] scrolled = abilityComp.getInventory().getScrollSlots();

        // --- 1. SCHRITT: SLOT-AUSWAHL ODER LÖSCHEN (LINKS) ---
        int keybindStartX = this.width / 2 - 131;
        int keybindStartHeight = this.height / 2 + 8;
        for (int i = 0; i < 4; ++i) {
            int slotY = keybindStartHeight + slotSpacing * i;

            if (mouseX >= keybindStartX && mouseX < keybindStartX + 22 && mouseY >= slotY && mouseY < slotY + 22) {
                this.selectedSlot = i;
                this.isKeybindButtonPressed = true;
                MinecraftClient.getInstance().player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0f, 1.0f);
                return true;
            }

            if (mouseX >= keybindStartX + 22 && mouseX < keybindStartX + 31 && mouseY >= slotY + 6 && mouseY < slotY + 16) {
                if (equipped[i] != null) {
                    ClientPlayNetworking.send(new EquipAbilityC2SPayload(-1, equipped[i], true));
                    MinecraftClient.getInstance().player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0f, 0.8f);
                }
                return true;
            }
        }

        // --- 2. SCHRITT: SLOT-AUSWAHL ODER LÖSCHEN (RECHTS) ---
        int rightSlotsStartX = this.width / 2 + 58;
        int rightSlotsStartHeight = this.height / 2 - 78;
        for (int i = 0; i < 8; ++i) {
            int slotY = rightSlotsStartHeight + slotSpacing * i;

            if (mouseX >= rightSlotsStartX && mouseX < rightSlotsStartX + 22 && mouseY >= slotY && mouseY < slotY + 22) {
                this.selectedSlot = i;
                this.isKeybindButtonPressed = false;
                MinecraftClient.getInstance().player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0f, 1.0f);
                return true;
            }

            if (mouseX >= rightSlotsStartX + 22 && mouseX < rightSlotsStartX + 31 && mouseY >= slotY + 6 && mouseY < slotY + 16) {
                if (scrolled[i] != null) {
                    ClientPlayNetworking.send(new EquipAbilityC2SPayload(-1, scrolled[i], false));
                    MinecraftClient.getInstance().player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0f, 0.8f);
                }
                return true;
            }
        }

        // --- 3. SCHRITT: FÄHIGKEIT IM GITTER ANKLICKEN (KORREKTUR: NUTZT EXAKT DIESELBE NACHRUTSCH-LOGIK WIE RENDER!) ---
        int abilityStartX = gridX + 2;
        int abilityStartHeight = gridY + 2;

        int currentWidth = abilityStartX;
        int currentHeight = abilityStartHeight;
        int currentSlot = 0;

        List<String> unlockedList = abilityComp.getInventory().getGridInventory().values().stream()
                .map(obj -> (String) obj)
                .toList();

        for (int i = 0; i < unlockedList.size(); ++i) {
            String abilityId = unlockedList.get(i);

            if (!abilityComp.getInventory().hasUnlocked(abilityId)) continue;

            boolean isEquipped = false;
            for (String eq : equipped) { if (abilityId.equals(eq)) isEquipped = true; }
            for (String scr : scrolled) { if (abilityId.equals(scr)) isEquipped = true; }
            if (isEquipped) continue;

            IAbility ability = ModAbilities.get(abilityId);

            if (ability != null) {
                int maxButtonsEachRow = 7;

                // KORREKTUR: Berechnet die Klickbox-Reihe haargenau nach derselben JJK-Formel wie beim Rendern!
                if (currentSlot % maxButtonsEachRow == 0 && currentSlot > 0) {
                    currentHeight += 19;
                    currentWidth = abilityStartX;
                }

                // Scannt den exakten, nachgerutschten Pixel-Bereich des Icons ab
                if (mouseX >= currentWidth - 1 && mouseX < currentWidth + 17 && mouseY >= currentHeight - 1 && mouseY < currentHeight + 17) {
                    if (!ability.isPassive()) {
                        ClientPlayNetworking.send(new EquipAbilityC2SPayload(this.selectedSlot, abilityId, this.isKeybindButtonPressed));
                        MinecraftClient.getInstance().player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0f, 1.2f);
                        return true;
                    }
                }

                ++currentSlot;
                currentWidth += 19;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void renderBackground(DrawContext drawContext, int mouseX, int mouseY, float delta) {
        drawContext.fill(0, 0, this.width, this.height, new Color(0, 0, 0, 40).getRGB());
    }

    @Override
    public boolean shouldPause() { return false; }
}