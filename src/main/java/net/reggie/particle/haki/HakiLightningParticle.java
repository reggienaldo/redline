package net.reggie.particle.haki;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;

public class HakiLightningParticle extends SpriteBillboardParticle {

    private final SpriteProvider spriteProvider;

    protected HakiLightningParticle(ClientWorld world, double x, double y, double z, double d, SpriteProvider spriteProvider) {
        super(world, x, y, z, 0.0, 0.0, 0.0);
        this.spriteProvider = spriteProvider;

        this.velocityMultiplier = 0.0F; // Blitze driften nicht wie Rauch weg
        this.maxAge = 8 + this.random.nextInt(8); // Kurze Lebensdauer für schnelles Flackern
        this.scale = 2.0F + this.random.nextFloat() * 1.5F; // Größe der Haki-Blitze

        // Zufälliger Wechsel zwischen intensivem Rot und bedrohlichem Schwarz/Dunkelrot
        if (this.random.nextBoolean()) {
            this.red = 0.9F + this.random.nextFloat() * 0.1F;
            this.green = 0.0F;
            this.blue = 0.0F;
        } else {
            this.red = 0.05F; // Nahezu Schwarz
            this.green = 0.0F;
            this.blue = 0.05F;
        }

        this.setSpriteForAge(spriteProvider);
    }

    @Override
    public void tick() {
        this.prevPosX = this.x;
        this.prevPosY = this.y;
        this.prevPosZ = this.z;

        if (this.age++ >= this.maxAge) {
            this.markDead();
            return;
        }

        // Zick-Zack-Verschiebung pro Tick für den Blitz-Effekt
        this.x += (this.random.nextDouble() - 0.5) * 0.25;
        this.y += (this.random.nextDouble() - 0.5) * 0.25;
        this.z += (this.random.nextDouble() - 0.5) * 0.25;

        this.setSpriteForAge(this.spriteProvider);
    }

    @Override
    public ParticleTextureSheet getType() {
        // Transluzent sorgt für saubere Transparenz-Übergänge bei Energie-VFX
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Factory implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientWorld world,
                                       double x, double y, double z,
                                       double vx, double vy, double vz) {

            return new HakiLightningParticle(world, x, y, z, vx, this.spriteProvider);
        }
    }
}