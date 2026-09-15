package net.reggie.mixin.client;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.reggie.Redline;
import net.reggie.game.haki.IHakiComponent;
import net.reggie.render.ModRenderLayers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntityRenderer.class)
public abstract class CombinedFirstPersonHakiMixin {

    // --- SCHWARZE HAKI TEXTUREN ---
    @Unique
    private static final Identifier HAKI_WIDE = Identifier.of(Redline.MOD_ID, "textures/entity/overlay/haki_arms_wide.png");
    @Unique
    private static final Identifier HAKI_SLIM = Identifier.of(Redline.MOD_ID, "textures/entity/overlay/haki_arms_slim.png");

    // --- BUNTE RYOU OUTLINES ---
    @Unique
    private static final Identifier[] GLOW_TEXTURES = {
            Identifier.of(Redline.MOD_ID, "textures/entity/overlay/haki_outline_red.png"),    // 0
            Identifier.of(Redline.MOD_ID, "textures/entity/overlay/haki_outline_blue.png"),   // 1
            Identifier.of(Redline.MOD_ID, "textures/entity/overlay/haki_outline_purple.png"), // 2
            Identifier.of(Redline.MOD_ID, "textures/entity/overlay/haki_outline_gold.png"),   // 3
            Identifier.of(Redline.MOD_ID, "textures/entity/overlay/haki_outline_cyan.png"),   // 4
            Identifier.of(Redline.MOD_ID, "textures/entity/overlay/haki_outline_green.png"),  // 5
            Identifier.of(Redline.MOD_ID, "textures/entity/overlay/haki_outline_orange.png")  // 6
    };

    @Inject(method = "renderLeftArm", at = @At("TAIL"))
    private void redline$renderCombinedLeftArm(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity player, CallbackInfo ci) {
        IHakiComponent hakiComp = Redline.HAKI.get(player);
        if (!hakiComp.isBusoActive()) return; // Nutzt das flache Interface

        PlayerEntityRenderer renderer = (PlayerEntityRenderer)(Object)this;
        PlayerEntityModel<AbstractClientPlayerEntity> model = renderer.getModel();

        ModelPart leftArmPart = model.leftArm;
        ModelPart leftSleevePart = model.leftSleeve;

        // 1. SCHICHT RENDERN: Tiefschwarz (Direkt auf dem Arm)
        boolean slim = player.getSkinTextures().model() == SkinTextures.Model.SLIM;
        Identifier blackTexture = slim ? HAKI_SLIM : HAKI_WIDE;
        VertexConsumer blackBuffer = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(blackTexture));
        leftArmPart.render(matrices, blackBuffer, light, OverlayTexture.DEFAULT_UV);

        // 2. SCHICHT RENDERN: Ryou Aura (Wird gezeichnet, wenn Ryou oder Internal Destruction freigeschaltet ist)
        if (hakiComp.isRyouUnlocked() || hakiComp.isInternalDestructionUnlocked()) {
            int colorIndex = MathHelper.clamp(hakiComp.getRyouColorIndex(), 0, GLOW_TEXTURES.length - 1);
            Identifier auraTexture = GLOW_TEXTURES[colorIndex];

            renderAuraLayer(matrices, vertexConsumers, leftArmPart, leftSleevePart, auraTexture);
        }
    }

    @Inject(method = "renderRightArm", at = @At("TAIL"))
    private void redline$renderCombinedRightArm(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity player, CallbackInfo ci) {
        IHakiComponent hakiComp = Redline.HAKI.get(player);
        if (!hakiComp.isBusoActive()) return; // Nutzt das flache Interface

        PlayerEntityRenderer renderer = (PlayerEntityRenderer)(Object)this;
        PlayerEntityModel<AbstractClientPlayerEntity> model = renderer.getModel();

        ModelPart rightArmPart = model.rightArm;
        ModelPart rightSleevePart = model.rightSleeve;

        // 1. SCHICHT RENDERN: Tiefschwarz (Direkt auf dem Arm)
        boolean slim = player.getSkinTextures().model() == SkinTextures.Model.SLIM;
        Identifier blackTexture = slim ? HAKI_SLIM : HAKI_WIDE;
        VertexConsumer blackBuffer = vertexConsumers.getBuffer(RenderLayer.getEntityTranslucent(blackTexture));
        rightArmPart.render(matrices, blackBuffer, light, OverlayTexture.DEFAULT_UV);

        // 2. SCHICHT RENDERN: Ryou Aura
        if (hakiComp.isRyouUnlocked() || hakiComp.isInternalDestructionUnlocked()) {
            int colorIndex = MathHelper.clamp(hakiComp.getRyouColorIndex(), 0, GLOW_TEXTURES.length - 1);
            Identifier auraTexture = GLOW_TEXTURES[colorIndex];

            renderAuraLayer(matrices, vertexConsumers, rightArmPart, rightSleevePart, auraTexture);
        }
    }

    @Unique
    private void renderAuraLayer(MatrixStack matrices, VertexConsumerProvider vertexConsumers, ModelPart arm, ModelPart sleeve, Identifier texture) {
        // Nutzt deine ModRenderLayers Registrierung
        VertexConsumer glowBuffer = vertexConsumers.getBuffer(ModRenderLayers.getInverseHull(texture));

        matrices.push();

        // Vergrößert die Aura-Schicht um 1.03x, damit das Schwarz darunter voll sichtbar bleibt
        matrices.scale(1.03f, 1.03f, 1.03f);
        matrices.translate(-0.03f, -0.04f, -0.04f);

        arm.render(matrices, glowBuffer, LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV);
        sleeve.render(matrices, glowBuffer, LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV);

        matrices.pop();
    }
}