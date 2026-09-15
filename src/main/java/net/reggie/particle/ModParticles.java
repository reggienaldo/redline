package net.reggie.particle;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.reggie.Redline;

public class ModParticles {

    public static final SimpleParticleType ENMA_SWEEP =
            registerParticle("enma_sweep", FabricParticleTypes.simple());

    public static final SimpleParticleType IMPACT =
            registerParticle("impact", FabricParticleTypes.simple());

    public static final SimpleParticleType CONQ_CLASH =
            registerParticle("conq_clash", FabricParticleTypes.simple());

    public static final SimpleParticleType CONQ_LIGHTNING =
            registerParticle("conq_lightning", FabricParticleTypes.simple());

    public static final SimpleParticleType RYOU_HIT =
            registerParticle("ryou_hit", FabricParticleTypes.simple());

    public static final SimpleParticleType CONQ_SPARK =
            registerParticle("bf_spark", FabricParticleTypes.simple());

    public static final SimpleParticleType HAKI_PARTIKEL = registerParticle("garp_conq_aura", FabricParticleTypes.simple());

    public static final SimpleParticleType CONQ_HIT =
            registerParticle("conq_hit", FabricParticleTypes.simple());

    public static final SimpleParticleType GARP_CONQ_AURA =
            registerParticle("garp_conq_aura", FabricParticleTypes.simple());

    public static final SimpleParticleType MAGMA_PARTICLE =
            registerParticle("magmaparticle", FabricParticleTypes.simple());

    public static final SimpleParticleType DARKNESS_PARTICLE =
            registerParticle("darknessparticle", FabricParticleTypes.simple());

    public static final SimpleParticleType IMPACT_2 =
            registerParticle("impact_2", FabricParticleTypes.simple());

    private static SimpleParticleType registerParticle(String name, SimpleParticleType particleType) {
        return Registry.register(Registries.PARTICLE_TYPE, Identifier.of(Redline.MOD_ID, name), particleType);
    }

    public static void registerParticles() {
        Redline.LOGGER.info("Registering Particles for " + Redline.MOD_ID);
    }
}
