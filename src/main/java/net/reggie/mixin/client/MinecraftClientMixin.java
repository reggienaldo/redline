package net.reggie.mixin.client;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.reggie.Redline;
import net.reggie.hud.AbilityHudRenderer;
import net.reggie.network.C2S.UseAbilityC2SPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Shadow public ClientPlayerEntity player;
    @Shadow public net.minecraft.client.option.GameOptions options; // Zugriff auf die Tastenbelegungen

    @Inject(method = "handleInputEvents", at = @At("HEAD"))
    private void onHandleInputEvents(CallbackInfo ci) {
        if (player != null) {
            // 1. Check, ob der Combat Mode überhaupt aktiv ist
            boolean combatActive = Redline.COMBAT_COMPONENT.get(player).isCombatModeEnabled();

            if (combatActive) {
                // 2. Horcht direkt auf die Rechtsklick-Taste von Minecraft, egal ob Hand voll oder leer!
                while (options.useKey.wasPressed()) {
                    // Erstellt das Payload-Paket für den Server (rechte Leiste = false)
                    UseAbilityC2SPayload packet = new UseAbilityC2SPayload(AbilityHudRenderer.abilityHudPointer, false);

                    // Schickt das Paket sofort ab
                    ClientPlayNetworking.send(packet);
                }
            }
        }
    }
}