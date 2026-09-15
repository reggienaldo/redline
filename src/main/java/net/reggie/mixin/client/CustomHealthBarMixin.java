package net.reggie.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.reggie.Redline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class CustomHealthBarMixin {

    @Unique
    private static final Identifier ICONS = Identifier.of(Redline.MOD_ID, "textures/gui/health_hud/icons.png");
    @Unique
    private static final Identifier CUSTOM_HEART = Identifier.of(Redline.MOD_ID, "textures/gui/health_hud/hearticon.png");

    @Unique
    private static final int HEART_SIZE = 9;

    @Unique
    private static final int TOTAL_FRAMES = 5;
    @Unique
    private int currentFrame = 1;
    @Unique
    private long lastUpdateTime = System.currentTimeMillis();
    @Unique
    private int lastTrackedHealth = -1;

    @Inject(method = "renderHealthBar", at = @At("HEAD"), cancellable = true)
    private void renderCustomHealthBar(
            DrawContext context, PlayerEntity player, int x, int y, int lines,
            int regeneratingHeartIndex, float maxHealth, int lastHealth,
            int health, int absorption, boolean blinking, CallbackInfo ci) {

        ci.cancel();

        MinecraftClient client = MinecraftClient.getInstance();
        TextRenderer textRenderer = client.textRenderer;

        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        int drawX = screenWidth / 2 - 91;
        int drawY = screenHeight - 39;

        RenderSystem.setShader(GameRenderer::getPositionTexProgram);

        int healthHalfHearts = (health + 1) / 2;
        int absorptionHalfHearts = (absorption + 1) / 2;

        boolean isBlinking = blinking && ((lastHealth / 3) % 2 == 1);

        int u, v;

        // 1) Leerherz
        RenderSystem.setShaderTexture(0, ICONS);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        context.drawTexture(ICONS, drawX, drawY, 52, 0, HEART_SIZE, HEART_SIZE, 256, 256);

        // 2) Entscheiden ob normal oder golden
        if (absorptionHalfHearts > 0) {
            v = 18; // GOLD
            u = (absorption > 1) ? 16 : 25;
        } else {
            v = 0;
            if (isBlinking) {
                u = 52;
            } else {
                u = (health > 1) ? 16 : 25;
            }
        }

        // Herz zeichnen
        if (!isBlinking) {
            RenderSystem.setShaderTexture(0, ICONS);
            context.drawTexture(ICONS, drawX, drawY, u, v, HEART_SIZE, HEART_SIZE, 256, 256);
        }

        // Custom Overlay nur bei normalen Herzen
        if (absorptionHalfHearts <= 0) {
            RenderSystem.setShaderTexture(0, CUSTOM_HEART);

            if (healthHalfHearts == 1) {
                int halfWidth = HEART_SIZE / 2;
                context.drawTexture(
                        CUSTOM_HEART,
                        drawX, drawY,
                        0, 0,
                        halfWidth, HEART_SIZE,
                        HEART_SIZE, HEART_SIZE
                );
            } else {
                context.drawTexture(
                        CUSTOM_HEART,
                        drawX, drawY,
                        0, 0,
                        HEART_SIZE, HEART_SIZE,
                        HEART_SIZE, HEART_SIZE
                );
            }
        }

        // Spark Animation
        int currentHealth = (int) player.getHealth();
        long currentTime = System.currentTimeMillis();

        if (currentHealth != lastTrackedHealth || currentTime - lastUpdateTime > 100L) {
            currentFrame++;
            if (currentFrame > TOTAL_FRAMES) currentFrame = 1;
            lastUpdateTime = currentTime;
            lastTrackedHealth = currentHealth;
        }

        Identifier sparkTexture = Identifier.of(
                Redline.MOD_ID,
                "textures/gui/health_hud/spark" + currentFrame + ".png"
        );

        RenderSystem.setShaderTexture(0, sparkTexture);
        RenderSystem.setShaderColor(0.1725F, 0.9098F, 0.9607F, 1F);
        context.drawTexture(sparkTexture, drawX - 3, drawY - 3, 0, 0, 15, 15, 15, 15);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        // =========================================================
        // HP TEXT (FIXED FOR DORIKI ATTRIBUTE INJECT)
        // =========================================================
        int absorptionAmount = (int) player.getAbsorptionAmount();

        // Holt das maximale Leben direkt aus dem Spieler (umgeht das blinkende MC-Render-maxHealth)
        int actualMaxHealth = (int) player.getMaxHealth();

        int totalHealth = currentHealth + absorptionAmount;
        int totalMaxHealth = actualMaxHealth + absorptionAmount;

        boolean hasAbsorption = absorptionAmount > 0;
        String color = hasAbsorption ? "§e" : "§c";

        String healthText = color + "ʜᴘ: " + totalHealth + "§7/" + color + totalMaxHealth;

        context.drawTextWithShadow(
                textRenderer,
                Text.literal(healthText),
                drawX + HEART_SIZE + 5,
                drawY + 1,
                0xFFFFFF
        );
    }
}
