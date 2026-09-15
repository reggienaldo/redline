package net.reggie.particle.haki;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;

@Environment(EnvType.CLIENT)
public class LightningParticle extends SpriteBillboardParticle {

    private final SpriteProvider spriteProvider;

    LightningParticle(ClientWorld world, double x, double y, double z, double d, SpriteProvider spriteProvider) {
        super(world, x, y, z, 0.0, 0.0, 0.0);
        this.spriteProvider = spriteProvider;

        // Exakt 7 Ticks Lebensdauer – passend zu deinen 7 Haki-Blitz-Phasen
        this.maxAge = 11;

        // Originalgetreue Farben der PNG-Datei beibehalten
        this.red = 1.0F;
        this.green = 1.0F;
        this.blue = 1.0F;

        // --- FIXED 64x64 SIZE ANPASSUNG ---
        // Ein Wert von 4.0F sorgt dafür, dass deine 64x64 Textur perfekt und gestochen scharf
        // über eine Höhe von genau 4 Blöcken skaliert wird, ohne die Pixel zu verzerren!
        this.scale = 4.0F;

        // Zufälliges Spiegeln (0 oder 180 Grad), um die Vielfalt der Zacken zu maximieren
        if (world.random.nextBoolean()) {
            this.angle = (float) Math.PI;
            this.prevAngle = this.angle;
        }

        this.setSpriteForAge(spriteProvider);
    }

    // Lässt die Haki-Blitze hell im Dunkeln glühen (Fullbright)
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
            // Schaltet jeden Tick auf die nächste Phase deiner 64x64 Textur um
            this.setSpriteForAge(this.spriteProvider);
        }
    }

    @Override
    public ParticleTextureSheet getType() {
        // Translucent ist zwingend erforderlich, damit die feinen, transparenten Pixel-Ränder
        // deiner 64x64 PNGs weich und ohne unschöne weiße Kästen gerendert werden
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
            return new LightningParticle(world, x, y, z, vx, this.spriteProvider);
        }
    }
}