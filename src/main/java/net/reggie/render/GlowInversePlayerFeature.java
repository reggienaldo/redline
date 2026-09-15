package net.reggie.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.reggie.Redline;
import net.reggie.game.haki.IHakiComponent;
import org.spongepowered.asm.mixin.Unique;

public class GlowInversePlayerFeature extends FeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {

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

    @Unique
    private final PlayerEntityModel<AbstractClientPlayerEntity> slimModel;
    @Unique
    private final PlayerEntityModel<AbstractClientPlayerEntity> wideModel;

    public GlowInversePlayerFeature(FeatureRendererContext<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> context) {
        super(context);
        MinecraftClient client = MinecraftClient.getInstance();

        this.wideModel = new PlayerEntityModel<>(client.getEntityModelLoader().getModelPart(EntityModelLayers.PLAYER), false);
        this.slimModel = new PlayerEntityModel<>(client.getEntityModelLoader().getModelPart(EntityModelLayers.PLAYER_SLIM), true);
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, AbstractClientPlayerEntity player,
                       float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {

        // Holt deine flache Haki-Komponente über den zentralen Redline-Key
        IHakiComponent hakiComp = Redline.HAKI.get(player);

        // Prüfen, ob das Rüstungshaki aktiv ist
        if (!hakiComp.isBusoActive()) return;

        // Prüfen, ob Ryou oder Internal Destruction freigeschaltet ist (Ersetzt das alte EvolutionStage-Enum)
        if (!hakiComp.isRyouUnlocked() && !hakiComp.isInternalDestructionUnlocked()) return;

        boolean slim = player.getSkinTextures().model() == SkinTextures.Model.SLIM;
        PlayerEntityModel<AbstractClientPlayerEntity> model = slim ? slimModel : wideModel;

        // Liest den Farb-Index direkt aus der flachen Haki-Komponente aus
        int index = MathHelper.clamp(hakiComp.getRyouColorIndex(), 0, GLOW_TEXTURES.length - 1);
        Identifier texture = GLOW_TEXTURES[index];

        PlayerEntityModel<AbstractClientPlayerEntity> contextModel = this.getContextModel();
        contextModel.copyBipedStateTo(model);

        // Arme + Sleeves exakt synchronisieren
        model.leftArm.copyTransform(contextModel.leftArm);
        model.rightArm.copyTransform(contextModel.rightArm);
        model.leftSleeve.copyTransform(contextModel.leftSleeve);
        model.rightSleeve.copyTransform(contextModel.rightSleeve);

        // RenderLayer.getEyes sorgt dafür, dass die Textur im Dunkeln leuchtet (Fullbright)
        VertexConsumer buffer = vertexConsumers.getBuffer(RenderLayer.getEyes(texture));

        int glowLight = LightmapTextureManager.MAX_LIGHT_COORDINATE;
        int overlay = OverlayTexture.DEFAULT_UV;

        matrices.push();

        // Aura leicht aufblasen, damit sie über dem Haki schwebt
        float glowScale = 1.02f;
        matrices.scale(glowScale, glowScale, glowScale);

        model.rightArm.render(matrices, buffer, glowLight, overlay);
        model.rightSleeve.render(matrices, buffer, glowLight, overlay);
        model.leftArm.render(matrices, buffer, glowLight, overlay);
        model.leftSleeve.render(matrices, buffer, glowLight, overlay);

        matrices.pop();
    }
}