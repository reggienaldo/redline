package net.reggie.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.reggie.Redline;
import net.reggie.hud.AbilityHudRenderer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MouseMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        if (client.player != null && client.currentScreen == null) {
            // Prüfen, ob der Combat Mode aktiv ist
            boolean combatActive = Redline.COMBAT_COMPONENT.get(client.player).isCombatModeEnabled();

            if (combatActive) {
                if (vertical != 0) {
                    // vertical > 0 = hochscrollen, < 0 = runterscrollen
                    if (vertical > 0) {
                        AbilityHudRenderer.abilityHudPointer--;
                    } else {
                        AbilityHudRenderer.abilityHudPointer++;
                    }

                    // Begrenze den Pointer exakt zwischen Slot 0 und Slot 7
                    if (AbilityHudRenderer.abilityHudPointer < 0) {
                        AbilityHudRenderer.abilityHudPointer = 7; // Rotiert nach unten
                    }
                    if (AbilityHudRenderer.abilityHudPointer > 7) {
                        AbilityHudRenderer.abilityHudPointer = 0; // Rotiert nach oben
                    }

                    // Blockiert das Standard-Minecraft-Hotbar-Scrollen
                    ci.cancel();
                }
            }
        }
    }
}
