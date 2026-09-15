package net.reggie.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.reggie.Redline;
import net.reggie.entity.custom.GomuArmsEntity;

public class ModEntities {

    public static final EntityType<GomuArmsEntity> GOMU_ARMS = Registry.register(
            Registries.ENTITY_TYPE, Identifier.of(Redline.MOD_ID, "gomu_arms"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, GomuArmsEntity::new)
                    .dimensions(EntityDimensions.fixed(1.5f, 1.75f)).build());

}
