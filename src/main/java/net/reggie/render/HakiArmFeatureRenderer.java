package net.reggie.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.reggie.Redline;
import net.reggie.game.haki.IHakiComponent;
import org.spongepowered.asm.mixin.Unique;

public class HakiArmFeatureRenderer extends FeatureRenderer<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> {

    // --- KLASSISCHES RÜSTUNGSHAKI (Schwarz) ---
    @Unique
    private static final Identifier HAKI_WIDE =
            Identifier.of(Redline.MOD_ID, "textures/entity/overlay/haki_arms_wide.png");

    @Unique
    private static final Identifier HAKI_SLIM =
            Identifier.of(Redline.MOD_ID, "textures/entity/overlay/haki_arms_slim.png");

    // --- KÖNIGSHAKI COATING (Rot/Violett Blitze) ---
    @Unique
    private static final Identifier HAKI_COATING_WIDE =
            Identifier.of(Redline.MOD_ID, "textures/entity/overlay/haki_coating_arms_wide.png");

    @Unique
    private static final Identifier HAKI_COATING_SLIM =
            Identifier.of(Redline.MOD_ID, "textures/entity/overlay/haki_coating_arms_slim.png");

    @Unique
    private final PlayerEntityModel<AbstractClientPlayerEntity> slimModel;
    @Unique
    private final PlayerEntityModel<AbstractClientPlayerEntity> wideModel;

    public HakiArmFeatureRenderer(
            FeatureRendererContext<AbstractClientPlayerEntity, PlayerEntityModel<AbstractClientPlayerEntity>> context) {
        super(context);

        MinecraftClient client = MinecraftClient.getInstance();

        this.wideModel = new PlayerEntityModel<>(
                client.getEntityModelLoader().getModelPart(EntityModelLayers.PLAYER),
                false
        );

        this.slimModel = new PlayerEntityModel<>(
                client.getEntityModelLoader().getModelPart(EntityModelLayers.PLAYER_SLIM),
                true
        );
    }

    @Override
    public void render(MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers,
                       int light,
                       AbstractClientPlayerEntity player,
                       float limbAngle,
                       float limbDistance,
                       float tickDelta,
                       float animationProgress,
                       float headYaw,
                       float headPitch) {

        // Holt deine flache Haki-Komponente über den zentralen Redline-Key
        IHakiComponent hakiComp = Redline.HAKI.get(player);

        // Wenn Rüstungshaki nicht aktiv ist, nichts rendern
        if (!hakiComp.isBusoActive()) return;

        boolean slim = player.getSkinTextures().model() == SkinTextures.Model.SLIM;
        PlayerEntityModel<AbstractClientPlayerEntity> model = slim ? slimModel : wideModel;

        // Visuelle Unterscheidung: Coating wird automatisch aktiv, wenn Buso an ist und Königshaki-Coating freigeschaltet ist
        boolean isCoatingActive = hakiComp.hasConquerorCoating();

        Identifier texture;
        if (isCoatingActive) {
            texture = slim ? HAKI_COATING_SLIM : HAKI_COATING_WIDE;
        } else {
            texture = slim ? HAKI_SLIM : HAKI_WIDE;
        }

        PlayerEntityModel<AbstractClientPlayerEntity> contextModel = this.getContextModel();

        // Alle Rotationen exakt kopieren
        contextModel.copyBipedStateTo(model);

        // Körperteile explizit syncen (wichtig für Swim/Crouch Pose)
        model.leftArm.copyTransform(contextModel.leftArm);
        model.rightArm.copyTransform(contextModel.rightArm);
        model.leftSleeve.copyTransform(contextModel.leftSleeve);
        model.rightSleeve.copyTransform(contextModel.rightSleeve);

        VertexConsumer buffer = vertexConsumers.getBuffer(
                RenderLayer.getEntityTranslucent(texture)
        );

        model.rightArm.render(matrices, buffer, light, OverlayTexture.DEFAULT_UV);
        model.leftArm.render(matrices, buffer, light, OverlayTexture.DEFAULT_UV);
    }
}