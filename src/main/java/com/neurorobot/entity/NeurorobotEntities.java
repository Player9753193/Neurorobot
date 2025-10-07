package com.neurorobot.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class NeurorobotEntities {
    public static final EntityType<NeurorobotEntity> NEUROROBOT = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of("neurorobot", "neurorobot"),
            FabricEntityTypeBuilder.create(SpawnGroup.CREATURE, NeurorobotEntity::new)
                    .dimensions(EntityDimensions.fixed(0.6f, 1.8f)) // Same size as a player
                    .build()
    );

    public static void registerEntities() {
        FabricDefaultAttributeRegistry.register(NEUROROBOT, NeurorobotEntity.createNeurorobotAttributes());
    }
}
