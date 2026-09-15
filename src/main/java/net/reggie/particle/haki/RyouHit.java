package net.reggie.particle.haki;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;

@Environment(EnvType.CLIENT)
public class RyouHit extends SpriteBillboardParticle {

    private final SpriteProvider spriteProvider;

    RyouHit(ClientWorld world, double x, double y, double z, double d, SpriteProvider spriteProvider) {
        super(world, x, y, z, 0.0, 0.0, 0.0);
        this.spriteProvider = spriteProvider;

        // Leicht längere Sichtbarkeit (8 Ticks) für den fließenden Ryou-Aura-Effekt
        this.maxAge = 8;

        // 1.0F stellt sicher, dass deine Custom-Texturfarben (z.B. deine gezeichnete blaue/rote Aura)
        // absolut originalgetreu und ungefiltert im Spiel gerendet werden.
        this.red = 1.0F;
        this.green = 1.0F;
        this.blue = 1.0F;

        // 🔥 DEUTLICH GRÖSSER: Basis-Skalierung von 1.3F auf 3.5F erhöht für einen wuchtigen Aura-Impact
        this.scale = 3.5F - (float)d * 0.1F;

        this.setSpriteForAge(spriteProvider);
    }

    // Lässt die Aura hell im Dunkeln glühen
    @Override
    public int getBrightness(float tint) {
        return 15728880;
    }

    @Override
    public void tick() {
        this.prevPosX = this.x;
        this.prevPosY = this.y;
        this.prevPosZ = this.z;

        if (this.age++ >= this.maxAge) {
            this.markDead();
        } else {
            this.setSpriteForAge(this.spriteProvider);
        }
    }

    // Perfekt für feine, transparente Aura-Effekte und Farbverläufe deiner PNG
    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Environment(EnvType.CLIENT)
    public static class Factory implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientWorld world,
                                       double x, double y, double z,
                                       double vx, double vy, double vz) {
            return new RyouHit(world, x, y, z, vx, this.spriteProvider);
        }
    }
}