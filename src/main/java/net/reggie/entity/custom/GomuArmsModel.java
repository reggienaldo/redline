package net.reggie.entity.custom;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;
import net.reggie.Redline;
import net.reggie.game.haki.IHakiComponent;
import software.bernie.geckolib.model.GeoModel;

public class GomuArmsModel extends GeoModel<GomuArmsEntity> {

    // Deine originalen Haki-Overlays aus deinem Feature-Renderer
    private static final Identifier HAKI_WIDE = Identifier.of(Redline.MOD_ID, "textures/entity/overlay/haki_arms_wide.png");
    private static final Identifier HAKI_SLIM = Identifier.of(Redline.MOD_ID, "textures/entity/overlay/haki_arms_slim.png");
    private static final Identifier HAKI_COATING_WIDE = Identifier.of(Redline.MOD_ID, "textures/entity/overlay/haki_coating_arms_wide.png");
    private static final Identifier HAKI_COATING_SLIM = Identifier.of(Redline.MOD_ID, "textures/entity/overlay/haki_coating_arms_slim.png");

    @Override
    public Identifier getModelResource(GomuArmsEntity animatable) {
        return Identifier.of(Redline.MOD_ID, "geo/gumgatling.geo.json");
    }

    @Override
    public Identifier getTextureResource(GomuArmsEntity animatable) {
        MinecraftClient client = MinecraftClient.getInstance();

        // Holt den reitenden Spieler, um sein Haki auszulesen
        if (animatable.getVehicle() instanceof AbstractClientPlayerEntity player) {
            IHakiComponent hakiComp = Redline.HAKI.get(player);

            // Wenn das Rüstungshaki aktiv ist, überschreiben wir die Basis-Textur der Arme sofort!
            if (hakiComp.isBusoActive()) {
                boolean slim = player.getSkinTextures().model() == SkinTextures.Model.SLIM;
                boolean isCoatingActive = hakiComp.hasConquerorCoating();

                if (isCoatingActive) {
                    return slim ? HAKI_COATING_SLIM : HAKI_COATING_WIDE;
                } else {
                    return slim ? HAKI_SLIM : HAKI_WIDE;
                }
            }
        }

        // FALLBACK: Wenn kein Haki aktiv ist, nutzen wir deinen normalen Skin für die Arme
        if (client.player != null) {
            return client.player.getSkinTextures().texture();
        }
        return Identifier.of("minecraft", "textures/entity/player/wide/steve.png");
    }

    @Override
    public Identifier getAnimationResource(GomuArmsEntity animatable) {
        return Identifier.of(Redline.MOD_ID, "animations/gumgatling.animation.json");
    }
}
