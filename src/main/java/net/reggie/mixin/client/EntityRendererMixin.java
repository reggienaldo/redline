package net.reggie.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity> {

    @Inject(method = "hasLabel", at = @At("HEAD"))
    private void redline$forceClientGlow(T entity, CallbackInfoReturnable<Boolean> cir) {
        // Minecraft bietet leider keine direkte "shouldGlow"-Methode im Renderer, aber wir können
        // uns hier einklinken, um den Glow-Zustand des Entities rein visuell auf dem Client zu manipulieren.
    }
}
