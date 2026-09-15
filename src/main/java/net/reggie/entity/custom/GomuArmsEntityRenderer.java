package net.reggie.entity.custom;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.reggie.Redline;
import net.reggie.game.haki.IHakiComponent;
import net.reggie.render.ModRenderLayers;
import software.bernie.geckolib.cache.object.*;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class GomuArmsEntityRenderer extends GeoEntityRenderer<GomuArmsEntity> {

    // --- DEINE BUNTE RYOU OUTLINE STRUKTUR (1:1 AUS DEINEM FEATURE COPIED) ---
    private static final Identifier[] GLOW_TEXTURES = {
            Identifier.of(Redline.MOD_ID, "textures/entity/overlay/haki_outline_red.png"),
            Identifier.of(Redline.MOD_ID, "textures/entity/overlay/haki_outline_blue.png"),
            Identifier.of(Redline.MOD_ID, "textures/entity/overlay/haki_outline_purple.png"),
            Identifier.of(Redline.MOD_ID, "textures/entity/overlay/haki_outline_gold.png"),
            Identifier.of(Redline.MOD_ID, "textures/entity/overlay/haki_outline_cyan.png"),
            Identifier.of(Redline.MOD_ID, "textures/entity/overlay/haki_outline_green.png"),
            Identifier.of(Redline.MOD_ID, "textures/entity/overlay/haki_outline_orange.png")
    };

    public GomuArmsEntityRenderer(EntityRendererFactory.Context renderManager) {
        super(renderManager, new GomuArmsModel());
    }

    @Override
    public Identifier getTextureLocation(GomuArmsEntity animatable) {
        return this.getGeoModel().getTextureResource(animatable);
    }

    @Override
    public void render(GomuArmsEntity animatable, float entityYaw, float partialTick,
                       MatrixStack matrices, VertexConsumerProvider vertexConsumers, int packedLight) {

        boolean hasPlayer = animatable.getVehicle() instanceof net.minecraft.client.network.AbstractClientPlayerEntity;
        AbstractClientPlayerEntity player = null;

        if (hasPlayer) {
            player = (AbstractClientPlayerEntity) animatable.getVehicle();
            IHakiComponent hakiComp = Redline.HAKI.get(player);

            // 1. HORIZONTAL-SYNC (Maus-Winkel links/rechts)
            entityYaw = player.getYaw();
            animatable.setYaw(player.getYaw());
            animatable.setHeadYaw(player.getYaw());
            animatable.setBodyYaw(player.getYaw());

            matrices.push();

            // 2. VERTIKAL-SYNC (Blickwinkel oben/unten)
            float playerPitch = player.getPitch();
            animatable.setPitch(playerPitch);

            // 3. MATRIZEN-POSEN FIX (HAARGENAU DEIN FEINTUNING-WERT)
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(playerPitch));
            matrices.translate(0.0, -1.0, 0.45);

            // Wenn Rüstungshaki an ist, verpassen wir den Gomu-Kuben den typischen metallischen Anime-Glanz
            if (hakiComp.isBusoActive()) {
                packedLight = LightmapTextureManager.MAX_LIGHT_COORDINATE;
            }
        }

        // --- BASE-RENDER: Zeichnet die nackten bzw. schwarzen Haki-Arme über dein Modell ---
        super.render(animatable, entityYaw, partialTick, matrices, vertexConsumers, packedLight);

        // =====================================================================
        // TRUE PRIME PIECE RYOU OVERLAY LAYER INJECTION (DEIN INVERSE HULL FIX)
        // =====================================================================
        if (hasPlayer && player != null) {
            IHakiComponent hakiComp = Redline.HAKI.get(player);

            // Nur rendern, wenn Buso aktiv ist UND Ryou oder Internal Destruction freigeschaltet wurde
            if (hakiComp.isBusoActive() && (hakiComp.isRyouUnlocked() || hakiComp.isInternalDestructionUnlocked())) {

                // Holt das fertig gebackene Geometrie-Modell aus dem Cache
                BakedGeoModel bakedModel = this.getGeoModel().getBakedModel(this.getGeoModel().getModelResource(animatable));

                if (bakedModel != null) {
                    // Liest den Farb-Index direkt aus der flachen Haki-Komponente aus
                    int index = MathHelper.clamp(hakiComp.getRyouColorIndex(), 0, GLOW_TEXTURES.length - 1);
                    Identifier texture = GLOW_TEXTURES[index];

                    // CORE FIX: Ruft jetzt exakt deinen eigenen Inverse Hull Shader für das additive Leuchten auf!
                    RenderLayer glowLayer = ModRenderLayers.getInverseHull(texture);
                    VertexConsumer buffer = vertexConsumers.getBuffer(glowLayer);
                    int glowLight = LightmapTextureManager.MAX_LIGHT_COORDINATE;

                    matrices.push();

                    // Rekursiver Loop zeichnet die leuchtende Outline haargenau über alle deine Teleskop-Knochen!
                    for (software.bernie.geckolib.cache.object.GeoBone topLevelBone : bakedModel.topLevelBones()) {
                        renderRyouGlowRecursive(matrices, topLevelBone, buffer, glowLight);
                    }

                    matrices.pop();
                }
            }
        }

        if (hasPlayer) {
            matrices.pop();
        }
    }

    // Hilfsmethode, um das Leuchten flüssig durch deine Knochen (Arm2, Arm3) zu jagen, ohne Z-Fighting/Flackern
    private void renderRyouGlowRecursive(MatrixStack matrices, software.bernie.geckolib.cache.object.GeoBone bone, VertexConsumer buffer, int light) {
        matrices.push();

        matrices.translate(bone.getPosX() / 16.0f, bone.getPosY() / 16.0f, bone.getPosZ() / 16.0f);
        matrices.multiply(new org.joml.Quaternionf().rotationXYZ(bone.getRotX(), bone.getRotY(), bone.getRotZ()));

        // INTERNER BLÄH-EFFECT: Schiebt die leuchtende Hülle um das 1.025-fache nach außen,
        // damit sie sauber über dem schwarzen Haki-Arm schwebt und sichtbar wird!
        matrices.scale(bone.getScaleX() * 1.025f, bone.getScaleY() * 1.025f, bone.getScaleZ() * 1.025f);

        if (!bone.isHidden()) {
            for (software.bernie.geckolib.cache.object.GeoCube cube : bone.getCubes()) {
                MatrixStack.Entry entry = matrices.peek();
                for (software.bernie.geckolib.cache.object.GeoQuad quad : cube.quads()) {
                    org.joml.Vector3f normal = entry.getNormalMatrix().transform(new org.joml.Vector3f(quad.direction().getUnitVector()));
                    for (software.bernie.geckolib.cache.object.GeoVertex vertex : quad.vertices()) {
                        org.joml.Vector4f position = entry.getPositionMatrix().transform(new org.joml.Vector4f(vertex.position().x() / 16.0f, vertex.position().y() / 16.0f, vertex.position().z() / 16.0f, 1.0f));

                        buffer.vertex(position.x(), position.y(), position.z())
                                .color(1.0f, 1.0f, 1.0f, 1.0f) // Volle Deckkraft für additives Leuchten
                                .texture(vertex.texU(), vertex.texV())
                                .overlay(OverlayTexture.DEFAULT_UV)
                                .light(light)
                                .normal(normal.x(), normal.y(), normal.z());
                    }
                }
            }
        }

        for (software.bernie.geckolib.cache.object.GeoBone childBone : bone.getChildBones()) {
            renderRyouGlowRecursive(matrices, childBone, buffer, light);
        }

        matrices.pop();
    }
}