package net.reggie;

import com.zigythebird.playeranim.animation.PlayerAnimationController;
import com.zigythebird.playeranim.animation.PlayerRawAnimationBuilder;
import com.zigythebird.playeranim.api.PlayerAnimationAccess;
import com.zigythebird.playeranim.api.PlayerAnimationFactory;
import com.zigythebird.playeranimcore.enums.PlayState;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.hadences.common.CustomBossBarManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.util.Identifier;
import net.reggie.entity.ModEntities;
import net.reggie.event.KeyInputHandler;
import net.reggie.hud.AbilityHudRenderer;
import net.reggie.hud.HakiBarRenderer;
import net.reggie.network.ModNetworking;
import net.reggie.particle.ModParticles;
import net.reggie.particle.haki.*;
import net.reggie.render.ModModelPredicates;
import net.reggie.entity.custom.GomuArmsEntityRenderer;

public class RedlineClient implements ClientModInitializer {

    public static final Identifier ANIMATION_LAYER_ID =
            Identifier.of(Redline.MOD_ID, "gumplayerbazooka");

    @Override
    public void onInitializeClient() {

        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(ANIMATION_LAYER_ID, 1000,
                player -> new PlayerAnimationController(player,
                        (controller, state, animSetter) -> PlayState.STOP
                )
        );


        ModNetworking.registerS2CPackets();

        CustomBossBarManager.initClient();

        ModModelPredicates.registerHakiWeapons();

        KeyInputHandler.register();

        ParticleFactoryRegistry.getInstance().register(ModParticles.CONQ_HIT, ConqHit.Factory::new);
        ParticleFactoryRegistry.getInstance().register(ModParticles.HAKI_PARTIKEL, HakiLightningParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(ModParticles.CONQ_SPARK, ConqHit.Factory::new);
        ParticleFactoryRegistry.getInstance().register(ModParticles.RYOU_HIT, RyouHit.Factory::new);
        ParticleFactoryRegistry.getInstance().register(ModParticles.CONQ_LIGHTNING, LightningParticle.Factory::new);
        ParticleFactoryRegistry.getInstance().register(ModParticles.IMPACT_2, Impact2.Factory::new);

        HudRenderCallback.EVENT.register(new HakiBarRenderer());
        HudRenderCallback.EVENT.register(new AbilityHudRenderer());


        EntityRendererRegistry.register(
                ModEntities.GOMU_ARMS, // Deine registrierte Entity-ID
                GomuArmsEntityRenderer::new
        );

    }

    public static void playPistol() {

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        AbstractClientPlayerEntity player = client.player;

        Object layer = PlayerAnimationAccess.getPlayerAnimationLayer(
                player,
                ANIMATION_LAYER_ID
        );

        if (!(layer instanceof PlayerAnimationController controller)) return;

        controller.triggerAnimation(
                Identifier.of(Redline.MOD_ID, "jetpistol")
        );
    }

    public static void playBazooka() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        AbstractClientPlayerEntity player = client.player;

        // Greift auf deinen registrierten Animations-Layer zu (Multiplayer- und Client-Safe!)
        Object layer = PlayerAnimationAccess.getPlayerAnimationLayer(
                player,
                RedlineClient.ANIMATION_LAYER_ID
        );

        if (!(layer instanceof PlayerAnimationController controller)) return;

        // WICHTIG: Erstellt die Animation über den PlayerRawAnimationBuilder,
        // damit die neuere Version deiner Library die Keyframes flüssig lädt
        var rawAnim = PlayerRawAnimationBuilder.begin()
                .thenPlay(Identifier.of(Redline.MOD_ID, "jetbazooka"))
                .build();

        // Feuert deine Gum-Gum-Bazooka-Animation sofort auf deinem Charaktermodell ab
        controller.triggerAnimation(rawAnim);
    }

    public static void playGatling() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        AbstractClientPlayerEntity player = client.player;
        Object layer = PlayerAnimationAccess.getPlayerAnimationLayer(player, RedlineClient.ANIMATION_LAYER_ID);

        if (!(layer instanceof PlayerAnimationController controller)) return;

        // WICHTIG: Erstellt eine LOOPENDE Animation für die Gatling-Dauer
        // "gatling" muss der Name INNERHALB deiner JSON-Animationsdatei sein!
        var rawAnim = PlayerRawAnimationBuilder.begin()
                .thenLoop(Identifier.of(Redline.MOD_ID, "jetgatling"))
                .build();

        controller.triggerAnimation(rawAnim);
    }

    public static void stopGatlingAnimation() {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        AbstractClientPlayerEntity player = client.player;
        Object layer = PlayerAnimationAccess.getPlayerAnimationLayer(player, RedlineClient.ANIMATION_LAYER_ID);

        if (layer instanceof PlayerAnimationController controller) {
            // EXAKT NACH DEINER DOKU: Stoppt die getriggerte Looping-Animation sofort flackerfrei!
            controller.stopTriggeredAnimation();
        }
    }

    public static void playHakiAnim(AbstractClientPlayerEntity targetPlayer, boolean hasWeapon) {
        if (targetPlayer == null) return;

        Object layer = PlayerAnimationAccess.getPlayerAnimationLayer(targetPlayer, ANIMATION_LAYER_ID);

        if (layer instanceof PlayerAnimationController controller) {
            // WICHTIG: Diese beiden Namen müssen exakt so als Animations-Keys in deiner JSON definiert sein!
            String animationName = hasWeapon ? "haki1" : "haki2";

            var animation = PlayerRawAnimationBuilder.begin()
                    .thenPlay(Identifier.of(Redline.MOD_ID, animationName))
                    .build();

            controller.triggerAnimation(animation);
        }
    }
}
