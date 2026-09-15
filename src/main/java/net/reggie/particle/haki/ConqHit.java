package net.reggie.particle.haki;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;

@Environment(EnvType.CLIENT)
public class ConqHit extends SpriteBillboardParticle {

    private final SpriteProvider spriteProvider;

    ConqHit(ClientWorld world, double x, double y, double z, double d, SpriteProvider spriteProvider) {
        super(world, x, y, z, 0.0, 0.0, 0.0);
        this.spriteProvider = spriteProvider;

        // 6 Frames für 6 Textur-Dateien = Absolut perfektes Timing
        this.maxAge = 6;

        // 1.0F auf allen Kanälen deaktiviert jegliche Einfärbung durch den Code.
        // Das Partikel wird exakt in deinen originalen PNG-Farben gerendert!
        this.red = 1.0F;
        this.green = 1.0F;
        this.blue = 1.0F;

        // 🔥 DEUTLICH GRÖSSER: Basis-Skalierung auf 4.0F angehoben, damit der Königshaki-Blitz absolut gewaltig einschlägt!
        this.scale = 4.0F - (float)d * 0.1F;

        this.setSpriteForAge(spriteProvider);
    }

    // Sorgt dafür, dass die Haki-Blitze im Dunkeln voll leuchten
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

    // TRANSLUCENT ist perfekt für deine eigenen halbtransparenten/schwarzen Effekte
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
            return new ConqHit(world, x, y, z, vx, this.spriteProvider);
        }
    }
}
