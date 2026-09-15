package net.reggie.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.reggie.Redline;
import net.reggie.hud.AbilityHudRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public class KeyboardSlotsMixin {

    @Shadow public ClientPlayerEntity player;

    // 1.21.1 CORE FIX: Wir hängen uns an den Anfang der Input-Verarbeitung von Minecraft
    @Inject(method = "handleInputEvents", at = @At("HEAD"))
    private void injectAbilityHotbarKeys(CallbackInfo ci) {
        MinecraftClient client = (MinecraftClient) (Object) this;

        if (player != null && client.currentScreen == null) {
            // Prüfen, ob der Combat Mode aktiv ist
            boolean combatActive = Redline.COMBAT_COMPONENT.get(player).isCombatModeEnabled();

            if (combatActive) {
                // Wir loopen durch die internen Keybindings von Minecraft für die Hotbar (Slot 1 bis 8)
                for (int i = 0; i < 8; i++) {
                    if (client.options.hotbarKeys[i].wasPressed()) {

                        // Setzt den Fähigkeiten-Pointer direkt auf die gedrückte Zahl (Zahl 1 = Index 0, etc.)
                        AbilityHudRenderer.abilityHudPointer = i;

                        // WICHTIG: Da wir die Tasten abgefangen haben, setzen wir das wasPressed-Flag
                        // von Minecraft manuell zurück, damit die normale Hotbar das Item NICHT wechselt!
                        while (client.options.hotbarKeys[i].wasPressed()) {
                            // Leert die Event-Warteschlange für diese Taste in diesem Frame
                        }
                    }
                }
            }
        }
    }
}
